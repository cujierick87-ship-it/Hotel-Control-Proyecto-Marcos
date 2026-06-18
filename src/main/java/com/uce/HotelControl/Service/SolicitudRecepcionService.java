package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.SolicitudRecepcion;
import com.uce.HotelControl.Repository.SolicitudRecepcionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SolicitudRecepcionService {

    @Autowired
    private SolicitudRecepcionRepository solicitudRecepcionRepository;

    // Guarda una solicitud, queja o comentario enviado por el recepcionista.
    // También asigna la fecha actual y el estado inicial PENDIENTE.
    public void guardarSolicitud(SolicitudRecepcion solicitud) {
        solicitud.setFechaEnvio(LocalDateTime.now());
        solicitud.setEstado("PENDIENTE");

        solicitudRecepcionRepository.save(solicitud);
    }

    // Cambia el estado de una solicitud a REVISADO.
// Se usa cuando el administrador ya leyó o atendió la solicitud.
    public void marcarComoRevisado(Long idSolicitud) {
        SolicitudRecepcion solicitud = solicitudRecepcionRepository.findById(idSolicitud).orElse(null);

        if (solicitud != null) {
            solicitud.setEstado("REVISADO");
            solicitudRecepcionRepository.save(solicitud);
        }
    }

    // Obtiene todas las solicitudes enviadas por recepción.
    // Este método se usará para que el administrador pueda revisarlas.
    public List<SolicitudRecepcion> obtenerTodas() {
        return solicitudRecepcionRepository.findAll();
    }
}
