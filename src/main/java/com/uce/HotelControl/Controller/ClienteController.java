package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.HabitacionService;
import com.uce.HotelControl.Service.ReservaService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
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

    @GetMapping("/cliente/habitacion/detalles/{id}")
    public String verDetallesHabitacion(@PathVariable Long id, Model model) {
        model.addAttribute("habitacion", habitacionService.obtenerPorId(id));

        List<Reserva> reservasHabitacion = reservaService.obtenerTodasLasReservas().stream()
                .filter(r -> r.getHabitacion().getIdHabitacion().equals(id))
                .filter(r -> !"CANCELADA".equalsIgnoreCase(r.getEstado()))
                .toList();

        model.addAttribute("reservas", reservasHabitacion);

        return "detalles_habitacion";
    }

    @GetMapping("/cliente/reserva/{id}")
    public String mostrarFormularioReserva(@PathVariable Long id, Model model, HttpSession session) {
        Usuario cliente = (Usuario) session.getAttribute("usuarioLogueado");

        if (cliente == null || !"CLIENTE".equalsIgnoreCase(cliente.getRol())) {
            return "redirect:/login";
        }

        model.addAttribute("habitacion", habitacionService.obtenerPorId(id));
        model.addAttribute("nuevaReserva", new Reserva());

        model.addAttribute("reservas", reservaService.obtenerTodasLasReservas().stream()
                .filter(r -> r.getHabitacion().getIdHabitacion().equals(id))
                .filter(r -> !"CANCELADA".equalsIgnoreCase(r.getEstado()))
                .toList());

        return "formulario_reserva";
    }

    @PostMapping("/cliente/reserva/guardar")
    public String guardarReserva(Reserva reserva, @RequestParam("idHab") Long idHab, HttpSession session) {
        Usuario cliente = (Usuario) session.getAttribute("usuarioLogueado");

        if (cliente == null || !"CLIENTE".equalsIgnoreCase(cliente.getRol())) {
            return "redirect:/login";
        }

        Habitacion hab = habitacionService.obtenerPorId(idHab);

        if (hab != null && !"MANTENIMIENTO".equalsIgnoreCase(hab.getEstado())) {
            reserva.setHabitacion(hab);

            // Estos datos ya no los escribe el cliente en el formulario.
            // Salen de la cuenta con la que inició sesión.
            reserva.setNombreCliente(cliente.getNombres());
            reserva.setApellidoCliente(cliente.getApellidos());
            reserva.setCedulaCliente(cliente.getCedula());
            reserva.setCorreo(cliente.getCorreo());
            reserva.setTelefono(cliente.getTelefono());

            reserva.setEstado("CONFIRMADA");

            reservaService.guardarReserva(reserva);
        }

        return "redirect:/cliente/inicio";
    }

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