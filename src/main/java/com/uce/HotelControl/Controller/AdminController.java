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

import com.uce.HotelControl.Model.InformacionHotel;
import com.uce.HotelControl.Model.Promocion;
import com.uce.HotelControl.Service.InformacionHotelService;
import com.uce.HotelControl.Service.PromocionService;

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
            @RequestParam("imagenArchivo") MultipartFile imagenArchivo)
            throws IOException {

        habitacionService.guardarHabitacion(nuevaHabitacion, imagenArchivo);
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
    public String darDeBaja(@PathVariable Long id) {
        habitacionService.darDeBaja(id);
        return "redirect:/admin/panel";
    }

    // Actualiza el estado de una habitación desde el panel administrador.
    // Sirve para cambiar estado operativo sin editar todo el formulario.
    @PostMapping("/admin/habitaciones/estado")
    public String actualizarEstadoHabitacion(Long idHabitacion, String estado) {
        habitacionService.actualizarEstado(idHabitacion, estado);
        return "redirect:/admin/panel";
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
    public String guardarPersonal(Usuario usuario) {
        usuarioService.guardarUsuario(usuario);
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
    public String eliminarPersonal(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
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
    public String marcarSolicitudRevisada(@PathVariable Long id) {
        solicitudRecepcionService.marcarComoRevisado(id);
        return "redirect:/admin/solicitudes";
    }

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

        model.addAttribute("totalReservas", reservaService.obtenerTodasLasReservas().size());
        model.addAttribute("ingresosTotales", reservaService.calcularIngresosValidos(reservaService.obtenerTodasLasReservas()));
        model.addAttribute("disponibles", disponibles);
        model.addAttribute("ocupadas", ocupadas);
        model.addAttribute("limpieza", limpieza);
        model.addAttribute("mantenimiento", mantenimiento);
        model.addAttribute("porcentajeOcupacion", porcentajeOcupacion);
        model.addAttribute("reservasCanceladas", reservaService.contarReservasPorEstado("CANCELADA"));
        model.addAttribute("reservasNoShow", reservaService.contarReservasPorEstado("NO-SHOW"));
        model.addAttribute("habitacionesMasReservadas", reservaService.obtenerHabitacionesMasReservadas());

        return "dashboard_admin";
    }

    @GetMapping("/admin/reportes")
    public String mostrarReportes(Model model) {
        model.addAttribute("reservas", reservaService.obtenerTodasLasReservas());
        model.addAttribute("ingresos", reservaService.calcularIngresosValidos(reservaService.obtenerTodasLasReservas()));
        return "reporte_admin";
    }

    @PostMapping("/admin/reportes/buscar")
    public String buscarReporte(LocalDate fechaInicio, LocalDate fechaFin, Model model) {
        model.addAttribute("reservas", reservaService.buscarReservasPorRango(fechaInicio, fechaFin));
        model.addAttribute("ingresos", reservaService.calcularIngresosValidos(reservaService.buscarReservasPorRango(fechaInicio, fechaFin)));
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "reporte_admin";
    }

    @GetMapping("/admin/clientes")
    public String clientesConReservas(Model model) {
        model.addAttribute("clientes", reservaService.obtenerClientesConReservas());
        model.addAttribute("reservasCliente", null);
        return "clientes_admin";
    }

    @PostMapping("/admin/clientes/buscar")
    public String buscarClienteReservas(String cedula, Model model) {
        model.addAttribute("clientes", reservaService.obtenerClientesConReservas());
        model.addAttribute("reservasCliente", reservaService.buscarPorCedula(cedula));
        model.addAttribute("cedulaBuscada", cedula);

        return "clientes_admin";
    }

    @GetMapping("/admin/institucional")
    public String panelInstitucional(Model model) {
        model.addAttribute("informacionHotel", informacionHotelService.obtenerInformacion());
        model.addAttribute("promocion", new Promocion());
        model.addAttribute("promociones", promocionService.obtenerTodas());

        return "admin_institucional";
    }

    @PostMapping("/admin/institucional/guardar-info")
    public String guardarInformacionHotel(@ModelAttribute InformacionHotel informacionHotel,
            @RequestParam("logoArchivo") MultipartFile logoArchivo)
            throws IOException {

        informacionHotelService.guardarInformacion(informacionHotel, logoArchivo);
        return "redirect:/admin/institucional";
    }

    @PostMapping("/admin/promociones/guardar")
    public String guardarPromocion(@ModelAttribute Promocion promocion,
            @RequestParam("imagenArchivo") MultipartFile imagenArchivo)
            throws IOException {

        promocionService.guardarPromocion(promocion, imagenArchivo);
        return "redirect:/admin/institucional";
    }

    @GetMapping("/admin/promociones/editar/{id}")
    public String editarPromocion(@PathVariable Long id, Model model) {
        model.addAttribute("informacionHotel", informacionHotelService.obtenerInformacion());
        model.addAttribute("promocion", promocionService.obtenerPorId(id));
        model.addAttribute("promociones", promocionService.obtenerTodas());

        return "admin_institucional";
    }

    @GetMapping("/admin/promociones/desactivar/{id}")
    public String desactivarPromocion(@PathVariable Long id) {
        promocionService.cambiarEstado(id, "INACTIVA");
        return "redirect:/admin/institucional";
    }

    @GetMapping("/admin/promociones/activar/{id}")
    public String activarPromocion(@PathVariable Long id) {
        promocionService.cambiarEstado(id, "ACTIVA");
        return "redirect:/admin/institucional";
    }
}
