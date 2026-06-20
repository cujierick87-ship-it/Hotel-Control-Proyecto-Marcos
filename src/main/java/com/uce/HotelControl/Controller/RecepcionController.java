package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Model.SolicitudRecepcion;
import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.HabitacionService;
import com.uce.HotelControl.Service.ReservaService;
import com.uce.HotelControl.Service.SolicitudRecepcionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RecepcionController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private SolicitudRecepcionService solicitudRecepcionService;

    // Carga el panel principal de recepción.
    // Muestra el estado de habitaciones y la lista de reservas registradas.
    @GetMapping("/recepcion/panel")
    public String panelRecepcion(Model model) {
        model.addAttribute("habitaciones", habitacionService.obtenerTodasLasHabitaciones());
        model.addAttribute("reservas", reservaService.obtenerTodasLasReservas());
        return "panel_recepcion";
    }

    // Busca reservas por código único o por cédula.
    // Después de buscar, mantiene activa la sección de reservas.
    @PostMapping("/recepcion/reservas/buscar")
    public String buscarReserva(String filtro, Model model) {
        model.addAttribute("habitaciones", habitacionService.obtenerTodasLasHabitaciones());
        model.addAttribute("reservas", reservaService.buscarPorCodigoOCedula(filtro));
        model.addAttribute("seccionActiva", "reservas");
        return "panel_recepcion";
    }

    // Procesa las acciones de recepción sobre una reserva.
    // Permite realizar CHECK-IN, CHECK-OUT o CANCELAR.
    @GetMapping("/recepcion/reservas/accion/{accion}/{id}")
    public String accionReserva(@PathVariable String accion, @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        reservaService.procesarAccionRecepcion(id, accion);
        redirectAttributes.addFlashAttribute("toastMensaje", "Reserva actualizada correctamente.");
        return "redirect:/recepcion/panel#reservas";
    }

    // Cambia una habitación de LIMPIEZA a DISPONIBLE.
    // Se usa cuando recepción confirma que la habitación ya fue limpiada.
    @GetMapping("/recepcion/habitaciones/limpiar/{id}")
    public String limpiarHabitacion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        habitacionService.marcarComoLimpia(id);
        redirectAttributes.addFlashAttribute("toastMensaje", "Habitacion marcada como disponible.");
        return "redirect:/recepcion/panel#limpieza";
    }

    // Muestra el formulario para enviar una solicitud, queja o comentario al administrador.
    @GetMapping("/recepcion/solicitud")
    public String mostrarSolicitud(Model model) {
        model.addAttribute("solicitud", new SolicitudRecepcion());
        return "solicitud_recepcion";
    }

    // Guarda la solicitud enviada por recepción.
    // Si existe usuario en sesión, guarda el nombre del recepcionista.
    @PostMapping("/recepcion/solicitud/guardar")
    public String guardarSolicitud(SolicitudRecepcion solicitud, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario != null) {
            String nombre = usuario.getNombres() + " " + usuario.getApellidos();

            if (nombre.trim().equals("null null") || nombre.trim().isEmpty()) {
                nombre = usuario.getNombreUsuario();
            }

            solicitud.setNombreRecepcionista(nombre);
        } else {
            solicitud.setNombreRecepcionista("Recepción");
        }

        solicitudRecepcionService.guardarSolicitud(solicitud);
        redirectAttributes.addFlashAttribute("toastMensaje", "Solicitud enviada al administrador.");
        return "redirect:/recepcion/panel";
    }

    // Muestra las habitaciones disponibles para iniciar una reserva presencial.
    // Esta vista debe ser reserva_presencial_habitaciones.html
    @GetMapping("/recepcion/reserva-presencial")
    public String mostrarHabitacionesPresencial(Model model) {
        model.addAttribute("habitaciones", habitacionService.obtenerHabitacionesDisponibles());
        return "reserva_presencial_habitaciones";
    }

    // Muestra el formulario de reserva presencial de una habitación seleccionada.
    // Carga la habitación, el formulario vacío y las reservas activas para bloquear fechas.
    @GetMapping("/recepcion/reserva-presencial/{id}")
    public String mostrarFormularioReservaPresencial(@PathVariable Long id, Model model) {
        model.addAttribute("habitacion", habitacionService.obtenerPorId(id));
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("reservas", reservaService.buscarReservasActivasPorHabitacion(id));
        return "reserva_presencial_recepcion";
    }

    // Guarda una reserva presencial hecha por el recepcionista.
    // Si las fechas no son válidas o hay cruce, vuelve al formulario con mensaje de error.
    @PostMapping("/recepcion/reserva-presencial/guardar")
    public String guardarReservaPresencial(Reserva reserva,
            @RequestParam("idHabitacion") Long idHabitacion,
            Model model,
            RedirectAttributes redirectAttributes) {

        Reserva guardada = reservaService.registrarReservaPresencial(reserva, idHabitacion);

        if (guardada == null) {
            model.addAttribute("error", "No se pudo registrar la reserva. Revise las fechas o la disponibilidad.");
            model.addAttribute("habitacion", habitacionService.obtenerPorId(idHabitacion));
            model.addAttribute("reserva", reserva);
            model.addAttribute("reservas", reservaService.buscarReservasActivasPorHabitacion(idHabitacion));
            return "reserva_presencial_recepcion";
        }

        redirectAttributes.addFlashAttribute("toastMensaje", "Reserva presencial registrada correctamente.");
        return "redirect:/recepcion/panel";
    }
}
