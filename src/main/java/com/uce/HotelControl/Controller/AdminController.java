package com.uce.HotelControl.Controller;
import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.HabitacionService;
import com.uce.HotelControl.Service.ReservaService;
import com.uce.HotelControl.Service.SolicitudRecepcionService;
import com.uce.HotelControl.Service.UsuarioService;
import java.io.IOException;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uce.HotelControl.Model.InformacionHotel;
import com.uce.HotelControl.Model.Promocion;
import com.uce.HotelControl.Service.InformacionHotelService;
import com.uce.HotelControl.Service.PromocionService;
import com.uce.HotelControl.Service.ComentarioReservaService;


@Controller
public class AdminController {

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SolicitudRecepcionService solicitudRecepcionService;

    @Autowired
    private InformacionHotelService informacionHotelService;

    @Autowired
    private PromocionService promocionService;

    @Autowired
    private ComentarioReservaService comentarioReservaService;

    // Carga el panel principal del administrador.
    // Muestra habitaciones y prepara el formulario para crear una nueva.
    @GetMapping("/admin/panel")
    public String panelAdmin(Model model) {
        model.addAttribute("habitaciones", habitacionService.obtenerTodasLasHabitaciones());
        model.addAttribute("nuevaHabitacion", new Habitacion());
        return "panel_admin";
    }

    // Guarda una habitación nueva o actualiza una existente.
    // También recibe la imagen subida desde la PC.
    @PostMapping("/admin/habitaciones/guardar")
    public String guardarHabitacion(@ModelAttribute Habitacion nuevaHabitacion,
            @RequestParam("imagenArchivo") MultipartFile imagenArchivo,
            RedirectAttributes redirectAttributes)
            throws IOException {

        boolean esEdicion = nuevaHabitacion.getIdHabitacion() != null;
        habitacionService.guardarHabitacion(nuevaHabitacion, imagenArchivo);
        redirectAttributes.addFlashAttribute("toastMensaje",
                esEdicion ? "Habitacion actualizada correctamente." : "Habitacion creada correctamente.");
        return "redirect:/admin/panel";
    }

    // Busca una habitación por número.
    // Muestra en la tabla solo las habitaciones encontradas.
    @PostMapping("/admin/habitaciones/buscar")
    public String buscarHabitacion(String numeroBuscado, Model model) {
        model.addAttribute("habitaciones", habitacionService.buscarPorNumero(numeroBuscado));
        model.addAttribute("nuevaHabitacion", new Habitacion());
        return "panel_admin";
    }

    // Carga una habitación en el formulario para editarla.
    // Permite modificar datos e imagen.
    @GetMapping("/admin/habitaciones/editar/{id}")
    public String editarHabitacion(@PathVariable Long id, Model model) {
        model.addAttribute("habitaciones", habitacionService.obtenerTodasLasHabitaciones());
        model.addAttribute("nuevaHabitacion", habitacionService.obtenerPorId(id));
        return "panel_admin";
    }

    // Da de baja una habitación.
    // Cambia su estado a MANTENIMIENTO.
    @GetMapping("/admin/habitaciones/baja/{id}")
    public String darDeBaja(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        habitacionService.darDeBaja(id);
        redirectAttributes.addFlashAttribute("toastMensaje", "Habitacion enviada a mantenimiento.");
        return "redirect:/admin/panel";
    }

    // Actualiza el estado de una habitación desde el panel administrador.
    // Sirve para cambiar estado operativo sin editar todo el formulario.
    @PostMapping("/admin/habitaciones/estado")
    public String actualizarEstadoHabitacion(Long idHabitacion, String estado, RedirectAttributes redirectAttributes) {
        habitacionService.actualizarEstado(idHabitacion, estado);
        redirectAttributes.addFlashAttribute("toastMensaje", "Estado de habitacion actualizado.");
        return "redirect:/admin/panel#estados";
    }

    // Muestra todas las reservas registradas.
    // Se usa para consulta general del administrador.
    @GetMapping("/admin/reservas")
    public String panelReservas(Model model) {
        model.addAttribute("reservas", reservaService.obtenerTodasLasReservas());
        return "panel_reservas";
    }

    // Muestra la pantalla de gestión de personal.
    // Carga usuarios y prepara el formulario para nuevo personal.
    @GetMapping("/admin/personal")
    public String panelPersonal(Model model) {
        model.addAttribute("usuarios", usuarioService.obtenerTodosLosUsuarios());
        model.addAttribute("nuevoUsuario", new Usuario());
        return "panel_personal";
    }

    // Guarda o actualiza un usuario del personal.
    // Se usa para administradores y recepcionistas.
    @PostMapping("/admin/personal/guardar")
    public String guardarPersonal(Usuario usuario, RedirectAttributes redirectAttributes) {
        boolean esEdicion = usuario.getIdUsuario() != null;
        usuarioService.guardarUsuario(usuario);
        redirectAttributes.addFlashAttribute("toastMensaje",
                esEdicion ? "Usuario actualizado correctamente." : "Usuario creado correctamente.");
        return "redirect:/admin/personal";
    }

    // Carga datos de un usuario en el formulario para editarlo.
    @GetMapping("/admin/personal/editar/{id}")
    public String editarPersonal(@PathVariable Long id, Model model) {
        model.addAttribute("usuarios", usuarioService.obtenerTodosLosUsuarios());
        model.addAttribute("nuevoUsuario", usuarioService.obtenerPorId(id));
        return "panel_personal";
    }

    // Elimina un usuario del personal.
    // En esta versión se elimina directamente de la base de datos.
    @GetMapping("/admin/personal/eliminar/{id}")
    public String eliminarPersonal(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.eliminarUsuario(id);
        redirectAttributes.addFlashAttribute("toastMensaje", "Usuario eliminado correctamente.");
        return "redirect:/admin/personal";
    }

    // Muestra las solicitudes, quejas y comentarios enviados por recepción.
    @GetMapping("/admin/solicitudes")
    public String verSolicitudes(Model model) {
        model.addAttribute("solicitudes", solicitudRecepcionService.obtenerTodas());
        return "panel_solicitudes";
    }

    // Marca una solicitud, queja o comentario como REVISADO.
    @GetMapping("/admin/solicitudes/revisar/{id}")
    public String marcarSolicitudRevisada(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        solicitudRecepcionService.marcarComoRevisado(id);
        redirectAttributes.addFlashAttribute("toastMensaje", "Solicitud marcada como revisada.");
        return "redirect:/admin/solicitudes";
    }

    // Muestra metricas generales de reservas, ingresos y ocupacion.
    @GetMapping("/admin/dashboard")
    public String dashboardAdmin(Model model) {
        int disponibles = habitacionService.contarPorEstado("DISPONIBLE");
        int ocupadas = habitacionService.contarPorEstado("OCUPADA");
        int limpieza = habitacionService.contarPorEstado("LIMPIEZA");
        int mantenimiento = habitacionService.contarPorEstado("MANTENIMIENTO");

        int totalHabitaciones = disponibles + ocupadas + limpieza + mantenimiento;
        double porcentajeOcupacion = 0;

        if (totalHabitaciones > 0) {
            porcentajeOcupacion = (ocupadas * 100.0) / totalHabitaciones;
        }

        double porcentajeDisponibles = totalHabitaciones > 0 ? (disponibles * 100.0) / totalHabitaciones : 0;
        double porcentajeLimpieza = totalHabitaciones > 0 ? (limpieza * 100.0) / totalHabitaciones : 0;
        double porcentajeMantenimiento = totalHabitaciones > 0 ? (mantenimiento * 100.0) / totalHabitaciones : 0;

        model.addAttribute("totalReservas", reservaService.obtenerTodasLasReservas().size());
        model.addAttribute("ingresosTotales", reservaService.calcularIngresosValidos(reservaService.obtenerTodasLasReservas()));
        model.addAttribute("disponibles", disponibles);
        model.addAttribute("ocupadas", ocupadas);
        model.addAttribute("limpieza", limpieza);
        model.addAttribute("mantenimiento", mantenimiento);
        model.addAttribute("porcentajeOcupacion", porcentajeOcupacion);
        model.addAttribute("porcentajeDisponibles", porcentajeDisponibles);
        model.addAttribute("porcentajeLimpieza", porcentajeLimpieza);
        model.addAttribute("porcentajeMantenimiento", porcentajeMantenimiento);
        model.addAttribute("reservasCanceladas", reservaService.contarReservasPorEstado("CANCELADA"));
        model.addAttribute("reservasNoShow", reservaService.contarReservasPorEstado("NO-SHOW"));
        model.addAttribute("habitacionesMasReservadas", reservaService.obtenerHabitacionesMasReservadas());
        model.addAttribute("resumenRecepcionistas", reservaService.obtenerResumenPorRecepcionista());
        return "dashboard_admin";
    }

    // Muestra el reporte general de reservas e ingresos.
    @GetMapping("/admin/reportes")
    public String mostrarReportes(Model model) {
        model.addAttribute("reservas", reservaService.obtenerTodasLasReservas());
        model.addAttribute("ingresos", reservaService.calcularIngresosValidos(reservaService.obtenerTodasLasReservas()));
        return "reporte_admin";
    }

    // Filtra reservas e ingresos por rango de fechas.
    @PostMapping("/admin/reportes/buscar")
    public String buscarReporte(LocalDate fechaInicio, LocalDate fechaFin, Model model) {
        model.addAttribute("reservas", reservaService.buscarReservasPorRango(fechaInicio, fechaFin));
        model.addAttribute("ingresos", reservaService.calcularIngresosValidos(reservaService.buscarReservasPorRango(fechaInicio, fechaFin)));
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "reporte_admin";
    }

    // Lista clientes que han realizado reservas.
    @GetMapping("/admin/clientes")
    public String clientesConReservas(Model model) {
        model.addAttribute("clientes", reservaService.obtenerClientesConReservas());
        model.addAttribute("reservasCliente", null);
        return "clientes_admin";
    }

    // Busca reservas de un cliente por cedula.
    @PostMapping("/admin/clientes/buscar")
    public String buscarClienteReservas(String cedula, Model model) {
        model.addAttribute("clientes", reservaService.obtenerClientesConReservas());
        model.addAttribute("reservasCliente", reservaService.buscarPorCedula(cedula));
        model.addAttribute("cedulaBuscada", cedula);

        return "clientes_admin";
    }

    // Muestra el modulo de informacion institucional y promociones.
    @GetMapping("/admin/institucional")
    public String panelInstitucional(Model model) {
        model.addAttribute("informacionHotel", informacionHotelService.obtenerInformacion());
        model.addAttribute("promocion", new Promocion());
        model.addAttribute("promociones", promocionService.obtenerTodas());

        return "admin_institucional";
    }

    // Guarda datos generales del hotel y su logo.
    @PostMapping("/admin/institucional/guardar-info")
    public String guardarInformacionHotel(@ModelAttribute InformacionHotel informacionHotel,
            @RequestParam("logoArchivo") MultipartFile logoArchivo,
            RedirectAttributes redirectAttributes)
            throws IOException {

        informacionHotelService.guardarInformacion(informacionHotel, logoArchivo);
        redirectAttributes.addFlashAttribute("toastMensaje", "Informacion institucional actualizada.");
        return "redirect:/admin/institucional";
    }

    // Guarda o actualiza promociones visuales.
    @PostMapping("/admin/promociones/guardar")
    public String guardarPromocion(@ModelAttribute Promocion promocion,
            @RequestParam("imagenArchivo") MultipartFile imagenArchivo,
            RedirectAttributes redirectAttributes)
            throws IOException {

        promocionService.guardarPromocion(promocion, imagenArchivo);
        redirectAttributes.addFlashAttribute("toastMensaje", "Promocion guardada correctamente.");
        return "redirect:/admin/institucional#promociones";
    }

    // Carga una promocion en el formulario para editarla.
    @GetMapping("/admin/promociones/editar/{id}")
    public String editarPromocion(@PathVariable Long id, Model model) {
        model.addAttribute("informacionHotel", informacionHotelService.obtenerInformacion());
        model.addAttribute("promocion", promocionService.obtenerPorId(id));
        model.addAttribute("promociones", promocionService.obtenerTodas());

        return "admin_institucional";
    }

    // Desactiva una promocion para ocultarla al cliente.
    @GetMapping("/admin/promociones/desactivar/{id}")
    public String desactivarPromocion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        promocionService.cambiarEstado(id, "INACTIVA");
        redirectAttributes.addFlashAttribute("toastMensaje", "Promocion desactivada.");
        return "redirect:/admin/institucional#promociones";
    }

    // Activa una promocion para mostrarla al cliente.
    @GetMapping("/admin/promociones/activar/{id}")
    public String activarPromocion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        promocionService.cambiarEstado(id, "ACTIVA");
        redirectAttributes.addFlashAttribute("toastMensaje", "Promocion activada.");
        return "redirect:/admin/institucional#promociones";
    }

    @GetMapping("/admin/resenas")
    public String verResenasHotel(Model model) {
        model.addAttribute("resenas", comentarioReservaService.obtenerTodos());
        model.addAttribute("positivas", comentarioReservaService.contarPorSentimiento("POSITIVO"));
        model.addAttribute("negativas", comentarioReservaService.contarPorSentimiento("NEGATIVO"));
        model.addAttribute("neutras", comentarioReservaService.contarPorSentimiento("NEUTRO"));
        model.addAttribute("alertasCriticas", comentarioReservaService.contarAlertasCriticas());
        model.addAttribute("categoriaMasAfectada", comentarioReservaService.obtenerCategoriaMasAfectada());
        model.addAttribute("totalResenas", comentarioReservaService.contarTotal());
        model.addAttribute("porcentajePositivo", comentarioReservaService.calcularPorcentajeSentimiento("POSITIVO"));
        model.addAttribute("porcentajeNegativo", comentarioReservaService.calcularPorcentajeSentimiento("NEGATIVO"));
        model.addAttribute("promedioCalificacion", comentarioReservaService.calcularPromedioCalificacion());
        model.addAttribute("resenasPorMes", comentarioReservaService.contarResenasPorMes());

        return "resenas_admin";
    }
}
