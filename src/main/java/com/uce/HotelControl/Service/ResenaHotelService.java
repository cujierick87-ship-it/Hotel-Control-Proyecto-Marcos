package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.ResenaHotel;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Repository.ResenaHotelRepository;
import com.uce.HotelControl.Repository.ReservaRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResenaHotelService {

    @Autowired
    private ResenaHotelRepository resenaHotelRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private AnalisisSentimientoService analisisSentimientoService;

    // Guarda una reseña si la reserva pertenece al cliente y ya fue finalizada.
    public ResenaHotel guardarResena(Long idReserva, String cedulaCliente, String comentarioTexto) {
        Reserva reserva = reservaRepository.findById(idReserva).orElse(null);

        if (reserva == null) {
            return null;
        }

        if (reserva.getCedulaCliente() == null || !reserva.getCedulaCliente().equals(cedulaCliente)) {
            return null;
        }

        if (!"FINALIZADA".equalsIgnoreCase(reserva.getEstado())) {
            return null;
        }

        if (resenaHotelRepository.existsByReservaIdReserva(idReserva)) {
            return null;
        }

        ResultadoAnalisisSentimiento resultado =
                analisisSentimientoService.analizarComentario(comentarioTexto);

        ResenaHotel resena = new ResenaHotel();
        resena.setReserva(reserva);
        resena.setComentarioTexto(comentarioTexto);
        resena.setSentimiento(resultado.getSentimiento());
        resena.setCategoriaAfectada(resultado.getCategoriaAfectada());
        resena.setAlertaCritica(resultado.getAlertaCritica());

        return resenaHotelRepository.save(resena);
    }

    // Verifica si una reserva ya tiene reseña.
    public boolean existeResenaParaReserva(Long idReserva) {
        return resenaHotelRepository.existsByReservaIdReserva(idReserva);
    }

    // Devuelve todas las reseñas para el administrador.
    public List<ResenaHotel> obtenerTodas() {
        return resenaHotelRepository.findAllByOrderByFechaRegistroDesc();
    }

    // Devuelve las reseñas visibles para los clientes.
    public List<ResenaHotel> obtenerResenasPublicas() {
        return resenaHotelRepository.findAllByOrderByFechaRegistroDesc();
    }

    // Cuenta reseñas según su sentimiento.
    public long contarPorSentimiento(String sentimiento) {
        return resenaHotelRepository.countBySentimiento(sentimiento);
    }

    // Cuenta alertas críticas.
    public long contarAlertasCriticas() {
        return resenaHotelRepository.countByAlertaCriticaTrue();
    }

    // Devuelve la categoría con más reseñas registradas.
    public String obtenerCategoriaMasAfectada() {
        String[] categorias = {"LIMPIEZA", "ATENCION", "INFRAESTRUCTURA", "COMODIDAD", "GENERAL"};

        String categoriaMayor = "GENERAL";
        long mayorCantidad = 0;

        for (String categoria : categorias) {
            long cantidad = resenaHotelRepository.countByCategoriaAfectada(categoria);

            if (cantidad > mayorCantidad) {
                mayorCantidad = cantidad;
                categoriaMayor = categoria;
            }
        }

        return categoriaMayor;
    }

    // Obtiene los IDs de reservas que ya tienen reseña.
    public List<Long> obtenerIdsReservasConResena(List<Reserva> reservas) {
        List<Long> ids = new ArrayList<>();

        for (Reserva reserva : reservas) {
            if (reserva.getIdReserva() != null
                    && resenaHotelRepository.existsByReservaIdReserva(reserva.getIdReserva())) {
                ids.add(reserva.getIdReserva());
            }
        }

        return ids;
    }
}