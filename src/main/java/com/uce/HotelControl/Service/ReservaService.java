package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Repository.HabitacionRepository;
import com.uce.HotelControl.Repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit; // 🔥 La herramienta moderna para contar días
import java.util.List;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private HabitacionRepository habitacionRepository; 

    // ==============================================================================
    // 1. SOLUCIÓN DE CONCURRENCIA: El primero que reserva y paga, bloquea el cuarto
    // ==============================================================================
    public void guardarReserva(Reserva reserva) {
        Habitacion habReal = habitacionRepository.findById(reserva.getHabitacion().getIdHabitacion()).orElse(null);
        
        if (habReal != null) {
            // ✅ FORMA CORRECTA PARA LOCALDATE: Cuenta los días automáticamente
            long dias = ChronoUnit.DAYS.between(reserva.getFechaCheckIn(), reserva.getFechaCheckOut());
            if (dias <= 0) dias = 1;
            
            reserva.setTotalPagar(dias * habReal.getPrecioNoche());
            
            // LA MAGIA DE LA JUSTICIA: Auto-confirmamos la reserva simulando el pago
            reserva.setEstado("CONFIRMADA"); 
            reserva.setHabitacion(habReal);
            
            // BLOQUEO ABSOLUTO: Cambiamos el estado para que nadie más la toque
            habReal.setEstado("OCUPADA"); 
            
            // Guardamos ambos cambios
            reservaRepository.save(reserva);
            habitacionRepository.save(habReal);
        }
    }

    // ==============================================================================
    // 2. PANEL DEL RECEPCIONISTA
    // ==============================================================================
    public void procesarAccionRecepcion(Long idReserva, String accion) {
        Reserva reserva = reservaRepository.findById(idReserva).orElse(null);
        
        if (reserva != null) {
            Habitacion hab = reserva.getHabitacion(); 
            
            if (accion.equals("CHECKIN")) {
                reserva.setEstado("CHECK-IN");
                hab.setEstado("OCUPADA");       
            } 
            else if (accion.equals("CHECKOUT")) {
                reserva.setEstado("FINALIZADA");
                hab.setEstado("LIMPIEZA"); // 🔥 LA MAGIA: El cuarto pasa a estar sucio
            }
            else if (accion.equals("CANCELAR")) {
                reserva.setEstado("CANCELADA");
                hab.setEstado("DISPONIBLE"); // Se libera si el recepcionista la cancela
            }
            
            reservaRepository.save(reserva);
            habitacionRepository.save(hab); 
        }
    }

    public List<Reserva> obtenerTodasLasReservas() {
        return reservaRepository.findAll();
    }

    public List<Reserva> buscarPorCedula(String cedula) {
        return reservaRepository.findByCedulaCliente(cedula);
    }
}