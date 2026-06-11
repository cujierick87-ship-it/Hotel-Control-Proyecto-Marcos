package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 *
 * @author Erick HC
 */
@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String inicio() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("nuevoUsuario", new Usuario());
        return "registro_cliente";
    }

    @PostMapping("/registro")
    public String procesarRegistro(Usuario nuevoUsuario, Model model) {

        boolean hayErrores = false;

        if (usuarioService.existeNombreUsuario(nuevoUsuario.getNombreUsuario())) {
            model.addAttribute("errorUsuario", "Este nombre de usuario ya está registrado.");
            hayErrores = true;
        }

        if (nuevoUsuario.getPasswordHash() == null || nuevoUsuario.getPasswordHash().length() < 6) {
            model.addAttribute("errorPassword", "La contraseña debe tener mínimo 6 caracteres.");
            hayErrores = true;
        }

        if (nuevoUsuario.getTelefono() == null || nuevoUsuario.getTelefono().length() < 10) {
            model.addAttribute("errorTelefono", "El teléfono debe tener mínimo 10 caracteres.");
            hayErrores = true;
        }

        if (hayErrores) {
            model.addAttribute("nuevoUsuario", nuevoUsuario);
            return "registro_cliente";
        }

        usuarioService.registrarCliente(nuevoUsuario);
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String procesarLogin(Usuario usuario, Model model, HttpSession session) {
        Usuario auth = usuarioService.validarLogin(
                usuario.getNombreUsuario(),
                usuario.getPasswordHash()
        );

        if (auth == null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            return "login";
        }

        // AQUÍ ESTABA EL PROBLEMA:
        // Guardamos el usuario en sesión para usarlo en otras pantallas.
        session.setAttribute("usuarioLogueado", auth);

        if (auth.getRol().equalsIgnoreCase("ADMINISTRADOR")) {
            return "redirect:/admin/panel";
        }

        if (auth.getRol().equalsIgnoreCase("RECEPCIONISTA")) {
            return "redirect:/recepcion/panel";
        }

        if (auth.getRol().equalsIgnoreCase("CLIENTE")) {
            return "redirect:/cliente/inicio";
        }

        model.addAttribute("error", "Rol no reconocido");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
