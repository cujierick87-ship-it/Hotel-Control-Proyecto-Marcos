package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.ComentarioReserva;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Repository.ComentarioReservaRepository;
import com.uce.HotelControl.Repository.ReservaRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComentarioReservaService {

    @Autowired
    private ComentarioReservaRepository comentarioReservaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private AnalisisSentimientoService analisisSentimientoService;

    // Busca una reserva finalizada del cliente que aun no tenga resena.
    public Reserva obtenerReservaFinalizadaSinComentario(String cedulaCliente) {
        List<Reserva> reservas = reservaRepository.findByCedulaCliente(cedulaCliente);

        for (Reserva reserva : reservas) {
            if ("FINALIZADA".equalsIgnoreCase(reserva.getEstado())
                    && !comentarioReservaRepository.existsByReservaId(reserva.getIdReserva())) {
                return reserva;
            }
        }

        return null;
    }

    // Guarda la resena solo si la reserva pertenece al cliente y esta finalizada.
    public ComentarioReserva guardarComentario(Long idReserva, String cedulaCliente, String comentarioTexto) {
        Reserva reserva = reservaRepository.findById(idReserva).orElse(null);

        if (reserva == null) {
            return null;
        }

        if (!reserva.getCedulaCliente().equals(cedulaCliente)) {
            return null;
        }

        if (!"FINALIZADA".equalsIgnoreCase(reserva.getEstado())) {
            return null;
        }

        if (comentarioReservaRepository.existsByReservaId(idReserva)) {
            return null;
        }

        ResultadoAnalisisSentimiento resultado =
                analisisSentimientoService.analizarComentario(comentarioTexto);

        ComentarioReserva comentario = new ComentarioReserva();
        comentario.setReservaId(idReserva);
        comentario.setNombreCliente(obtenerNombreVisible(reserva));
        comentario.setComentarioTexto(comentarioTexto);
        comentario.setSentimiento(resultado.getSentimiento());
        comentario.setCategoriaAfectada(resultado.getCategoriaAfectada());
        comentario.setAlertaCritica(resultado.getAlertaCritica());

        return comentarioReservaRepository.save(comentario);
    }

    public List<ComentarioReserva> obtenerTodos() {
        return comentarioReservaRepository.findAllByOrderByFechaRegistroDesc();
    }

    public long contarPorSentimiento(String sentimiento) {
        return comentarioReservaRepository.countBySentimiento(sentimiento);
    }

    // Cuenta todas las resenas registradas.
    public long contarTotal() {
        return comentarioReservaRepository.count();
    }

    // Calcula el porcentaje de un sentimiento sobre el total de resenas.
    public double calcularPorcentajeSentimiento(String sentimiento) {
        long total = contarTotal();

        if (total == 0) {
            return 0;
        }

        return (contarPorSentimiento(sentimiento) * 100.0) / total;
    }

    // Estima una calificacion promedio usando el sentimiento detectado.
    public double calcularPromedioCalificacion() {
        List<ComentarioReserva> resenas = comentarioReservaRepository.findAll();

        if (resenas.isEmpty()) {
            return 0;
        }

        int suma = 0;

        for (ComentarioReserva resena : resenas) {
            if ("POSITIVO".equalsIgnoreCase(resena.getSentimiento())) {
                suma += 5;
            } else if ("NEGATIVO".equalsIgnoreCase(resena.getSentimiento())) {
                suma += 1;
            } else {
                suma += 3;
            }
        }

        return suma * 1.0 / resenas.size();
    }

    // Agrupa resenas por mes para graficar la evolucion de opiniones.
    public Map<String, Integer> contarResenasPorMes() {
        Map<String, Integer> conteo = new LinkedHashMap<>();

        for (ComentarioReserva resena : comentarioReservaRepository.findAll()) {
            if (resena.getFechaRegistro() == null) {
                continue;
            }

            String mes = resena.getFechaRegistro().getYear()
                    + "-"
                    + String.format("%02d", resena.getFechaRegistro().getMonthValue());

            conteo.put(mes, conteo.getOrDefault(mes, 0) + 1);
        }

        return conteo;
    }

    public long contarAlertasCriticas() {
        return comentarioReservaRepository.countByAlertaCriticaTrue();
    }

    public String obtenerCategoriaMasAfectada() {
        String[] categorias = {"LIMPIEZA", "ATENCION", "INFRAESTRUCTURA", "COMODIDAD", "GENERAL"};

        String categoriaMayor = "GENERAL";
        long mayor = 0;

        for (String categoria : categorias) {
            long cantidad = comentarioReservaRepository.countByCategoriaAfectada(categoria);

            if (cantidad > mayor) {
                mayor = cantidad;
                categoriaMayor = categoria;
            }
        }

        return categoriaMayor;
    }

    private String obtenerNombreVisible(Reserva reserva) {
        String nombre = reserva.getNombreCliente();
        String apellido = reserva.getApellidoCliente();

        if (nombre == null || nombre.isBlank()) {
            nombre = "Huesped";
        }

        if (apellido == null || apellido.isBlank()) {
            return nombre;
        }

        return nombre + " " + apellido;
    }
}
