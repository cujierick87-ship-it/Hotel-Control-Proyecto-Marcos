package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.ComentarioReserva;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Repository.ComentarioReservaRepository;
import com.uce.HotelControl.Repository.ReservaRepository;
import java.util.List;
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
