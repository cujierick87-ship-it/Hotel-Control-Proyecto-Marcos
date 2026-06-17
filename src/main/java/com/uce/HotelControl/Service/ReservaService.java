package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Repository.HabitacionRepository;
import com.uce.HotelControl.Repository.ReservaRepository;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    // Guarda una reserva hecha por el cliente
    public void guardarReserva(Reserva reserva) {
        Habitacion habitacion = habitacionRepository
                .findById(reserva.getHabitacion().getIdHabitacion())
                .orElse(null);

        if (habitacion != null) {

            long dias = ChronoUnit.DAYS.between(
                    reserva.getFechaCheckIn(),
                    reserva.getFechaCheckOut()
            );

            if (dias <= 0) {
                dias = 1;
            }

            reserva.setTotalPagar(dias * habitacion.getPrecioNoche());

            // La reserva web queda confirmada automáticamente
            reserva.setEstado("CONFIRMADA");

            // Se conecta la reserva con la habitación real de la BD
            reserva.setHabitacion(habitacion);

            /*
             IMPORTANTE:
             Aquí NO ponemos la habitación en OCUPADA.
             La habitación solo pasa a OCUPADA cuando recepción hace Check-In.
            */

            reservaRepository.save(reserva);
        }
    }

    // Acciones del recepcionista: Check-In, Check-Out o Cancelar
    public void procesarAccionRecepcion(Long idReserva, String accion) {
        Reserva reserva = reservaRepository.findById(idReserva).orElse(null);

        if (reserva != null) {
            Habitacion habitacion = reserva.getHabitacion();

            if (accion.equals("CHECKIN")) {
                reserva.setEstado("CHECK-IN");
                habitacion.setEstado("OCUPADA");
            }

            if (accion.equals("CHECKOUT")) {
                reserva.setEstado("FINALIZADA");
                habitacion.setEstado("LIMPIEZA");
            }

            if (accion.equals("CANCELAR")) {
                reserva.setEstado("CANCELADA");
            }

            reservaRepository.save(reserva);
            habitacionRepository.save(habitacion);
        }
    }

    public List<Reserva> obtenerTodasLasReservas() {
        return reservaRepository.findAll();
    }

    public List<Reserva> buscarPorCedula(String cedula) {
        return reservaRepository.findByCedulaCliente(cedula);
    }
}