package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.UsuarioService;
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

    // --- 1. CARGA DE VISTAS ---
    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro_cliente";
    }

    // --- 2. PROCESAMIENTO DE LOGIN ---
    @PostMapping("/login")
    public String procesarLogin(Usuario usuario, Model model) {
        Usuario auth = usuarioService.validarLogin(usuario.getNombreUsuario(), usuario.getPasswordHash());

        if (auth == null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            return "login";
        }

        // Sin sesiones, solo redirigimos según el rol
        return switch (auth.getRol().toUpperCase()) {
            case "ADMINISTRADOR" -> "redirect:/admin/panel";
            case "RECEPCIONISTA" -> "redirect:/recepcion/panel";
            case "CLIENTE" -> "redirect:/cliente/inicio";
            default -> {
                model.addAttribute("error", "Rol no reconocido");
                yield "login";
            }
        };
    }
}