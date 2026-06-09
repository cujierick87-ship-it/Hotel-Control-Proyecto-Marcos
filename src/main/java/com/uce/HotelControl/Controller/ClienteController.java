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

@Controller
public class ClienteController {

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private ReservaService reservaService;

    @GetMapping("/cliente/inicio")
    public String inicioCliente(Model model) {
        model.addAttribute("habitaciones", habitacionService.obtenerTodasLasHabitaciones());
        return "inicio_cliente";
    }

    @GetMapping("/cliente/reserva/{id}")
    public String mostrarFormularioReserva(@PathVariable Long id, Model model) {
        // Solo enviamos el objeto vacio, el cliente llenara todo
        model.addAttribute("habitacion", habitacionService.obtenerPorId(id));
        model.addAttribute("nuevaReserva", new Reserva());
        return "formulario_reserva";
    }

    @PostMapping("/cliente/reserva/guardar")
    public String guardarReserva(Reserva reserva, @RequestParam("idHab") Long idHab) {
        // Buscamos la habitacion
        Habitacion hab = habitacionService.obtenerPorId(idHab);
        
        // Guardamos directo sin validaciones complejas
        if ("DISPONIBLE".equals(hab.getEstado())) {
            reserva.setHabitacion(hab);
            reservaService.guardarReserva(reserva);
        }
        
        return "redirect:/cliente/inicio";
    }

    @GetMapping("/cliente/historial")
    public String verHistorial(Model model) {
        // Traemos todas las reservas, el HTML se encargara de filtrar
        model.addAttribute("listaCompleta", reservaService.obtenerTodasLasReservas());
        return "historial_cliente";
    }
    
    //// Para ver el detaale de la habitacion
    // NUEVO: Ver detalles de la habitación antes de reservar
    @GetMapping("/cliente/habitacion/detalles/{id}")
    public String verDetallesHabitacion(@PathVariable Long id, Model model) {
        // Buscamos la habitación y la mandamos a la nueva vista
        model.addAttribute("habitacion", habitacionService.obtenerPorId(id));
        return "detalles_habitacion"; // Crearemos este archivo ahora
    }
}