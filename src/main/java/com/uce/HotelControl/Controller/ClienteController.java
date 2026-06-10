package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Service.HabitacionService;
import com.uce.HotelControl.Service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// ¡Este import es la clave para que no dé error la lista de reservas!
import java.util.List;

@Controller
public class ClienteController {

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private ReservaService reservaService;

    // 1. Mostrar el catálogo
    @GetMapping("/cliente/inicio")
    public String inicioCliente(Model model) {
        model.addAttribute("habitaciones", habitacionService.obtenerTodasLasHabitaciones());
        return "inicio_cliente";
    }

    // 2. Ver Detalles y enviar datos al Calendario
    @GetMapping("/cliente/habitacion/detalles/{id}")
    public String verDetallesHabitacion(@PathVariable Long id, Model model) {
        model.addAttribute("habitacion", habitacionService.obtenerPorId(id));

        // Filtramos las reservas de esta habitación para el calendario
        List<Reserva> reservasHabitacion = reservaService.obtenerTodasLasReservas().stream()
                .filter(r -> r.getHabitacion().getIdHabitacion().equals(id))
                .filter(r -> !"CANCELADA".equalsIgnoreCase(r.getEstado()))
                .toList();

        model.addAttribute("reservas", reservasHabitacion);

        return "detalles_habitacion";
    }

    // 3. Mostrar Formulario de Reserva (Versión Simplificada)
    @GetMapping("/cliente/reserva/{id}")
    public String mostrarFormularioReserva(@PathVariable Long id, Model model) {
        model.addAttribute("habitacion", habitacionService.obtenerPorId(id));
        model.addAttribute("nuevaReserva", new Reserva());

        // Filtramos y enviamos las reservas válidas de esta habitación en una sola línea
        model.addAttribute("reservas", reservaService.obtenerTodasLasReservas().stream()
                .filter(r -> r.getHabitacion().getIdHabitacion().equals(id) && !"CANCELADA".equalsIgnoreCase(r.getEstado()))
                .toList());

        return "formulario_reserva";
    }

    // 4. Guardar la Reserva en la Base de Datos (CORREGIDO)
    @PostMapping("/cliente/reserva/guardar")
    public String guardarReserva(Reserva reserva, @RequestParam("idHab") Long idHab) {
        Habitacion hab = habitacionService.obtenerPorId(idHab);
        
        // Eliminamos la restricción estricta de "DISPONIBLE" porque ahora permitimos 
        // múltiples reservas en distintas fechas. El calendario ya bloquea los días ocupados.
        if (hab != null && !"MANTENIMIENTO".equalsIgnoreCase(hab.getEstado())) {
            reserva.setHabitacion(hab);
            
            // Forzamos un estado inicial a la reserva si llega vacío, 
            // asegurando que el recepcionista la pueda listar en su panel.
            if (reserva.getEstado() == null || reserva.getEstado().isEmpty()) {
                reserva.setEstado("CONFIRMADA"); // O "PENDIENTE", según tu lógica de negocio
            }
            
            reservaService.guardarReserva(reserva);
        }
        
        return "redirect:/cliente/inicio";
    }

    // 5. Ver Historial
    @GetMapping("/cliente/historial")
    public String verHistorial(Model model) {
        model.addAttribute("listaCompleta", reservaService.obtenerTodasLasReservas());
        return "historial_cliente";
    }
}
