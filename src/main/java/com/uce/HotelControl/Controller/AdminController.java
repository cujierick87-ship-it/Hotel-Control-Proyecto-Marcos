package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.HabitacionService;
import com.uce.HotelControl.Service.ReservaService;
import com.uce.HotelControl.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 *
 * @author Erick HC
 */
@Controller
public class AdminController {

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private UsuarioService usuarioService;

    // 1. CARGAR EL PANEL NORMAL
    @GetMapping("/admin/panel")
    public String panelAdmin(Model model) {
        model.addAttribute("habitaciones", habitacionService.obtenerTodasLasHabitaciones());
        model.addAttribute("nuevaHabitacion", new Habitacion());
        return "panel_admin";
    }

    // 2. GUARDAR O ACTUALIZAR
    @PostMapping("/admin/habitaciones/guardar")
    public String guardarHabitacion(Habitacion nuevaHabitacion) {
        habitacionService.guardarHabitacion(nuevaHabitacion);
        return "redirect:/admin/panel";
    }

    // 3. BUSCAR POR NÚMERO (Usamos un POST simple desde el HTML)
    @PostMapping("/admin/habitaciones/buscar")
    public String buscarHabitacion(String numeroBuscado, Model model) {
        model.addAttribute("habitaciones", habitacionService.buscarPorNumero(numeroBuscado));
        model.addAttribute("nuevaHabitacion", new Habitacion());
        return "panel_admin";
    }

    // 4. CARGAR DATOS EN EL FORMULARIO PARA EDITAR
    @GetMapping("/admin/habitaciones/editar/{id}")
    public String editarHabitacion(@PathVariable Long id, Model model) {
        model.addAttribute("habitaciones", habitacionService.obtenerTodasLasHabitaciones());
        model.addAttribute("nuevaHabitacion", habitacionService.obtenerPorId(id));
        return "panel_admin";
    }

    // 5. DAR DE BAJA
    @GetMapping("/admin/habitaciones/baja/{id}")
    public String darDeBaja(@PathVariable Long id) {
        habitacionService.darDeBaja(id);
        return "redirect:/admin/panel";
    }

    @GetMapping("/admin/reservas")
    public String panelReservas(Model model) {
        model.addAttribute("reservas", reservaService.obtenerTodasLasReservas());
        return "panel_reservas";
    }

    // 1. Mostrar la pantalla de personal
    @GetMapping("/admin/personal")
    public String panelPersonal(Model model) {
        model.addAttribute("usuarios", usuarioService.obtenerTodosLosUsuarios());
        model.addAttribute("nuevoUsuario", new Usuario());
        return "panel_personal";
    }

    // 2. Guardar o actualizar un empleado
    @PostMapping("/admin/personal/guardar")
    public String guardarPersonal(Usuario usuario) {
        usuarioService.guardarUsuario(usuario);
        return "redirect:/admin/personal";
    }

    // 3. Cargar datos en el formulario para editar
    @GetMapping("/admin/personal/editar/{id}")
    public String editarPersonal(@PathVariable Long id, Model model) {
        model.addAttribute("usuarios", usuarioService.obtenerTodosLosUsuarios());
        model.addAttribute("nuevoUsuario", usuarioService.obtenerPorId(id));
        return "panel_personal";
    }

    // 4. Eliminar / Inhabilitar cuenta
    @GetMapping("/admin/personal/eliminar/{id}")
    public String eliminarPersonal(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return "redirect:/admin/personal";
    }
}
