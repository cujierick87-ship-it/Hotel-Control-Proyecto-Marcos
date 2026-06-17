package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.HabitacionService;
import com.uce.HotelControl.Service.ReservaService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;
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

     // Muestra el panel principal del cliente.
    // Valida que exista una sesión activa de cliente.
    @GetMapping("/cliente/inicio")
    public String inicioCliente(Model model, HttpSession session) {
        Usuario cliente = (Usuario) session.getAttribute("usuarioLogueado");

        if (cliente == null || !"CLIENTE".equalsIgnoreCase(cliente.getRol())) {
            return "redirect:/login";
        }

        model.addAttribute("habitaciones", habitacionService.obtenerHabitacionesDisponibles());
        return "inicio_cliente";
    }

    // Muestra los detalles de una habitación.
    // Si la habitación está en mantenimiento, no permite verla.
    @GetMapping("/cliente/habitacion/detalles/{id}")
    public String verDetallesHabitacion(@PathVariable Long id, Model model) {
        Habitacion habitacion = habitacionService.obtenerPorId(id);

        if (habitacion == null || habitacion.getIdHabitacion() == null) {
            return "redirect:/cliente/inicio";
        }

        if ("MANTENIMIENTO".equalsIgnoreCase(habitacion.getEstado())) {
            return "redirect:/cliente/inicio";
        }

        List<Reserva> reservasHabitacion = reservaService.obtenerTodasLasReservas()
                .stream()
                .filter(r -> r.getHabitacion().getIdHabitacion().equals(id))
                .filter(r -> !"CANCELADA".equalsIgnoreCase(r.getEstado()))
                .collect(Collectors.toList());

        model.addAttribute("habitacion", habitacion);
        model.addAttribute("reservas", reservasHabitacion);

        return "detalles_habitacion";
    }

    // Muestra el formulario para reservar una habitación.
    // Bloquea el acceso si la habitación está en mantenimiento.
    @GetMapping("/cliente/reserva/{id}")
    public String mostrarFormularioReserva(@PathVariable Long id, Model model, HttpSession session) {
        Usuario cliente = (Usuario) session.getAttribute("usuarioLogueado");

        if (cliente == null || !"CLIENTE".equalsIgnoreCase(cliente.getRol())) {
            return "redirect:/login";
        }

        Habitacion habitacion = habitacionService.obtenerPorId(id);

        if (habitacion == null || habitacion.getIdHabitacion() == null) {
            return "redirect:/cliente/inicio";
        }

        if ("MANTENIMIENTO".equalsIgnoreCase(habitacion.getEstado())) {
            return "redirect:/cliente/inicio";
        }

        List<Reserva> reservasHabitacion = reservaService.obtenerTodasLasReservas()
                .stream()
                .filter(r -> r.getHabitacion().getIdHabitacion().equals(id))
                .filter(r -> !"CANCELADA".equalsIgnoreCase(r.getEstado()))
                .collect(Collectors.toList());

        model.addAttribute("habitacion", habitacion);
        model.addAttribute("nuevaReserva", new Reserva());
        model.addAttribute("reservas", reservasHabitacion);

        return "formulario_reserva";
    }

    // Guarda la reserva del cliente.
    // Toma los datos personales desde la sesión y no permite reservar habitaciones en mantenimiento.
    @PostMapping("/cliente/reserva/guardar")
    public String guardarReserva(Reserva reserva, @RequestParam("idHab") Long idHab, HttpSession session) {
        Usuario cliente = (Usuario) session.getAttribute("usuarioLogueado");

        if (cliente == null || !"CLIENTE".equalsIgnoreCase(cliente.getRol())) {
            return "redirect:/login";
        }

        Habitacion hab = habitacionService.obtenerPorId(idHab);

        if (hab == null || hab.getIdHabitacion() == null) {
            return "redirect:/cliente/inicio";
        }

        if ("MANTENIMIENTO".equalsIgnoreCase(hab.getEstado())) {
            return "redirect:/cliente/inicio";
        }

        reserva.setHabitacion(hab);
        reserva.setNombreCliente(cliente.getNombres());
        reserva.setApellidoCliente(cliente.getApellidos());
        reserva.setCedulaCliente(cliente.getCedula());
        reserva.setCorreo(cliente.getCorreo());
        reserva.setTelefono(cliente.getTelefono());
        reserva.setEstado("CONFIRMADA");

        reservaService.guardarReserva(reserva);

        return "redirect:/cliente/inicio";
    }

    // Muestra el historial de reservas del cliente.
    // Busca las reservas usando la cédula del cliente logueado.
    @GetMapping("/cliente/historial")
    public String verHistorial(Model model, HttpSession session) {
        Usuario cliente = (Usuario) session.getAttribute("usuarioLogueado");

        if (cliente == null || !"CLIENTE".equalsIgnoreCase(cliente.getRol())) {
            return "redirect:/login";
        }

        model.addAttribute("listaCompleta", reservaService.buscarPorCedula(cliente.getCedula()));
        return "historial_cliente";
    }
}