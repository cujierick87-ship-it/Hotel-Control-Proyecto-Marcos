package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.HabitacionService;
import com.uce.HotelControl.Service.ReservaService;
import com.uce.HotelControl.Service.UsuarioService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AdminController {

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private UsuarioService usuarioService;

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
}