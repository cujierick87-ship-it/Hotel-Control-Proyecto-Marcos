
package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.HabitacionService;
import com.uce.HotelControl.Service.ReservaService;
import com.uce.HotelControl.Service.UsuarioService;
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

    @Autowired
    private UsuarioService usuarioService;

    // Verifica si existe un cliente logueado en sesión.
    // Si no existe o el rol no es CLIENTE, devuelve null.
    private Usuario obtenerClienteSesion(HttpSession session) {
        Usuario cliente = (Usuario) session.getAttribute("usuarioLogueado");

        if (cliente == null) {
            return null;
        }

        if (!"CLIENTE".equalsIgnoreCase(cliente.getRol())) {
            return null;
        }

        return cliente;
    }

    // Muestra el panel principal del cliente.
    // Solo permite entrar si existe una sesión activa de cliente.
    @GetMapping("/cliente/inicio")
    public String inicioCliente(Model model, HttpSession session) {
        Usuario cliente = obtenerClienteSesion(session);

        if (cliente == null) {
            return "redirect:/login?sesion=expirada";
        }

        model.addAttribute("habitaciones", habitacionService.obtenerHabitacionesDisponibles());
        return "inicio_cliente";
    }

    // Muestra los detalles de una habitación.
    // Valida sesión y bloquea habitaciones en mantenimiento.
    @GetMapping("/cliente/habitacion/detalles/{id}")
    public String verDetallesHabitacion(@PathVariable Long id, Model model, HttpSession session) {
        Usuario cliente = obtenerClienteSesion(session);

        if (cliente == null) {
            return "redirect:/login?sesion=expirada";
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
        model.addAttribute("reservas", reservasHabitacion);

        return "detalles_habitacion";
    }

    // Muestra el formulario para reservar una habitación.
    // Valida sesión, habitación existente y que no esté en mantenimiento.
    @GetMapping("/cliente/reserva/{id}")
    public String mostrarFormularioReserva(@PathVariable Long id, Model model, HttpSession session) {
        Usuario cliente = obtenerClienteSesion(session);

        if (cliente == null) {
            return "redirect:/login?sesion=expirada";
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
    // Toma automáticamente los datos personales desde la cuenta registrada.
    @PostMapping("/cliente/reserva/guardar")
    public String guardarReserva(Reserva reserva, @RequestParam("idHab") Long idHab, HttpSession session) {
        Usuario cliente = obtenerClienteSesion(session);

        if (cliente == null) {
            return "redirect:/login?sesion=expirada";
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

        Reserva reservaGuardada = reservaService.guardarReserva(reserva);

        if (reservaGuardada == null) {
            return "redirect:/cliente/inicio";
        }

        return "redirect:/cliente/comprobante/" + reservaGuardada.getIdReserva();
    }

    // Muestra el historial de reservas del cliente.
    // Busca las reservas usando la cédula del cliente logueado.
    @GetMapping("/cliente/historial")
    public String verHistorial(Model model, HttpSession session) {
        Usuario cliente = obtenerClienteSesion(session);

        if (cliente == null) {
            return "redirect:/login?sesion=expirada";
        }

        model.addAttribute("listaCompleta", reservaService.buscarPorCedula(cliente.getCedula()));
        return "historial_cliente";
    }

    // Muestra el comprobante de una reserva.
    // Valida que la reserva pertenezca al cliente logueado.
    @GetMapping("/cliente/comprobante/{id}")
    public String verComprobante(@PathVariable Long id, HttpSession session, Model model) {
        Usuario cliente = obtenerClienteSesion(session);

        if (cliente == null) {
            return "redirect:/login?sesion=expirada";
        }

        Reserva reserva = reservaService.obtenerPorId(id);

        if (reserva == null) {
            return "redirect:/cliente/historial";
        }

        if (!reserva.getCedulaCliente().equals(cliente.getCedula())) {
            return "redirect:/cliente/historial";
        }

        model.addAttribute("reserva", reserva);
        return "comprobante_reserva";
    }

    // Cancela una reserva del cliente.
    // Solo funciona si la reserva está CONFIRMADA.
    @GetMapping("/cliente/reserva/cancelar/{id}")
    public String cancelarReservaCliente(@PathVariable Long id, HttpSession session) {
        Usuario cliente = obtenerClienteSesion(session);

        if (cliente == null) {
            return "redirect:/login?sesion=expirada";
        }

        reservaService.cancelarReservaCliente(id, cliente.getCedula());
        return "redirect:/cliente/historial";
    }

    // Muestra el perfil del cliente.
    // Permite visualizar sus datos personales.
    @GetMapping("/cliente/perfil")
    public String verPerfil(HttpSession session, Model model) {
        Usuario cliente = obtenerClienteSesion(session);

        if (cliente == null) {
            return "redirect:/login?sesion=expirada";
        }

        model.addAttribute("cliente", cliente);
        return "perfil_cliente";
    }

    // Actualiza los datos personales del cliente.
    // Valida que el teléfono tenga mínimo 10 caracteres.
    @PostMapping("/cliente/perfil/actualizar")
    public String actualizarPerfil(Usuario datosPerfil, HttpSession session, Model model) {
        Usuario cliente = obtenerClienteSesion(session);

        if (cliente == null) {
            return "redirect:/login?sesion=expirada";
        }

        if (datosPerfil.getTelefono() == null || datosPerfil.getTelefono().length() < 10) {
            model.addAttribute("errorTelefono", "El teléfono debe tener mínimo 10 caracteres.");
            model.addAttribute("cliente", datosPerfil);
            return "perfil_cliente";
        }

        Usuario actualizado = usuarioService.actualizarPerfilCliente(
                cliente.getIdUsuario(),
                datosPerfil
        );

        session.setAttribute("usuarioLogueado", actualizado);

        model.addAttribute("cliente", actualizado);
        model.addAttribute("mensaje", "Perfil actualizado correctamente.");

        return "perfil_cliente";
    }
}