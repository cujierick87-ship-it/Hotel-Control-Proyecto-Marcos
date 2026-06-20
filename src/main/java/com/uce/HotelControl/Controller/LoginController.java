package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    // Redirige la ruta principal hacia la pantalla de login.
    @GetMapping("/")
    public String inicio() {
        return "redirect:/login";
    }

    // Muestra la pantalla de inicio de sesión.
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    // Muestra el formulario para registrar un cliente nuevo.
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("nuevoUsuario", new Usuario());
        return "registro_cliente";
    }

    // Procesa el registro del cliente.
    // Valida usuario repetido, contraseña mínima y teléfono mínimo antes de guardar.
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

    // Procesa el inicio de sesion y redirige segun el rol del usuario.
    @PostMapping("/login")
    public String procesarLogin(Usuario usuario, Model model, HttpServletRequest request) {
        Usuario auth = usuarioService.validarLogin(
                usuario.getNombreUsuario(),
                usuario.getPasswordHash()
        );

        if (auth == null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            return "login";
        }

        // Limpia cualquier sesion anterior para evitar datos mezclados entre roles.
        HttpSession sesionAnterior = request.getSession(false);
        if (sesionAnterior != null) {
            sesionAnterior.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("usuarioLogueado", auth);
        session.setMaxInactiveInterval(30 * 60);

        if (auth.getRol().equalsIgnoreCase("ADMINISTRADOR")) {
            if (Boolean.TRUE.equals(auth.getRequiereCambioPassword())) {
                return "redirect:/cambiar-password-inicial";
            }

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
    // Muestra la pantalla para cambiar la contraseña inicial del administrador.
    // Solo entra aquí si el usuario es administrador y requiere cambiar contraseña.
    @GetMapping("/cambiar-password-inicial")
    public String mostrarCambioPasswordInicial(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!usuario.getRol().equalsIgnoreCase("ADMINISTRADOR")) {
            return "redirect:/login";
        }

        if (!Boolean.TRUE.equals(usuario.getRequiereCambioPassword())) {
            return "redirect:/admin/panel";
        }

        return "cambiar_password_inicial";
    }

    // Procesa el cambio de contraseña inicial del administrador.
    // Valida contraseña actual, longitud mínima y confirmación de contraseña.
    @PostMapping("/cambiar-password-inicial")
    public String cambiarPasswordInicial(String passwordActual,
                                          String nuevaPassword,
                                          String confirmarPassword,
                                          HttpSession session,
                                          Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        if (!usuario.getPasswordHash().equals(passwordActual)) {
            model.addAttribute("errorActual", "La contraseña actual no es correcta.");
            return "cambiar_password_inicial";
        }

        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            model.addAttribute("errorNueva", "La nueva contraseña debe tener mínimo 6 caracteres.");
            return "cambiar_password_inicial";
        }

        if (!nuevaPassword.equals(confirmarPassword)) {
            model.addAttribute("errorConfirmar", "Las contraseñas no coinciden.");
            return "cambiar_password_inicial";
        }

        Usuario actualizado = usuarioService.cambiarPasswordInicial(usuario.getIdUsuario(), nuevaPassword);
        session.setAttribute("usuarioLogueado", actualizado);

        return "redirect:/admin/panel";
    }

    // Cierra la sesión del usuario.
    // Borra los datos guardados en sesión y retorna al login.
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}