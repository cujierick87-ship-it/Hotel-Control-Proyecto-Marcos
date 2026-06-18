package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Repository.HabitacionRepository;
import com.uce.HotelControl.Repository.ReservaRepository;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

// Guarda una reserva hecha por el cliente.
// Calcula el total, genera código único y registra la reserva en estado CONFIRMADA.
    public Reserva guardarReserva(Reserva reserva) {
        Habitacion habitacion = habitacionRepository
                .findById(reserva.getHabitacion().getIdHabitacion())
                .orElse(null);

        if (habitacion == null) {
            return null;
        }

        if ("MANTENIMIENTO".equalsIgnoreCase(habitacion.getEstado())) {
            return null;
        }

        if (reserva.getFechaCheckIn() == null || reserva.getFechaCheckOut() == null) {
            return null;
        }

        if (!reserva.getFechaCheckOut().isAfter(reserva.getFechaCheckIn())) {
            return null;
        }

        List<Reserva> choques = reservaRepository.encontrarChoquesDeFechas(
                habitacion.getIdHabitacion(),
                reserva.getFechaCheckIn(),
                reserva.getFechaCheckOut()
        );

        if (!choques.isEmpty()) {
            return null;
        }

        long dias = ChronoUnit.DAYS.between(
                reserva.getFechaCheckIn(),
                reserva.getFechaCheckOut()
        );

        if (dias <= 0) {
            dias = 1;
        }

        reserva.setTotalPagar(dias * habitacion.getPrecioNoche());

        reserva.setEstado("CONFIRMADA");

        reserva.setHabitacion(habitacion);

        if (reserva.getCodigoReserva() == null || reserva.getCodigoReserva().isEmpty()) {
            String codigo = "HC-" + java.util.UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();

            reserva.setCodigoReserva(codigo);
        }

        return reservaRepository.save(reserva);
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

    // Busca una reserva por ID.
    // Se usa para mostrar comprobante y cancelar reservas.
    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id).orElse(null);
    }

    // Cancela una reserva del cliente.
    // Solo permite cancelar si la reserva todavía está CONFIRMADA.
    public boolean cancelarReservaCliente(Long idReserva, String cedulaCliente) {
        Reserva reserva = reservaRepository.findById(idReserva).orElse(null);

        if (reserva == null) {
            return false;
        }

        if (!reserva.getCedulaCliente().equals(cedulaCliente)) {
            return false;
        }

        if (!"CONFIRMADA".equalsIgnoreCase(reserva.getEstado())) {
            return false;
        }

        reserva.setEstado("CANCELADA");
        reservaRepository.save(reserva);
        return true;
    }

    // Busca reservas por código único o por cédula.
// Si encuentra por código, devuelve solo esa reserva.
// Si no encuentra por código, busca por cédula.
    public List<Reserva> buscarPorCodigoOCedula(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return reservaRepository.findAll();
        }

        Reserva porCodigo = reservaRepository.findByCodigoReserva(filtro.trim());

        if (porCodigo != null) {
            return java.util.Collections.singletonList(porCodigo);
        }

        return reservaRepository.findByCedulaCliente(filtro.trim());
    }

// Obtiene las reservas activas de una habitación.
// Se usa para pintar de rojo las fechas ocupadas en reserva presencial.
    public List<Reserva> buscarReservasActivasPorHabitacion(Long idHabitacion) {
        return reservaRepository.buscarReservasActivasPorHabitacion(idHabitacion);
    }

// Registra una reserva presencial desde recepción.
// Reutiliza guardarReserva para validar fechas, calcular total y generar código.
    public Reserva registrarReservaPresencial(Reserva reserva, Long idHabitacion) {
        Habitacion habitacion = habitacionRepository.findById(idHabitacion).orElse(null);

        if (habitacion == null) {
            return null;
        }

        reserva.setHabitacion(habitacion);

        return guardarReserva(reserva);
    }

}
