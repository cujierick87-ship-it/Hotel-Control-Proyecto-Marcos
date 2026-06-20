package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    // Devuelve la pagina principal del usuario segun su rol.
    private String redirigirSegunRol(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        if (usuario.getRol().equalsIgnoreCase("ADMINISTRADOR")) {
            if (Boolean.TRUE.equals(usuario.getRequiereCambioPassword())) {
                return "redirect:/cambiar-password-inicial";
            }

            return "redirect:/admin/panel";
        }

        if (usuario.getRol().equalsIgnoreCase("RECEPCIONISTA")) {
            return "redirect:/recepcion/panel";
        }

        if (usuario.getRol().equalsIgnoreCase("CLIENTE")) {
            return "redirect:/cliente/inicio";
        }

        return null;
    }

    // Redirige la ruta principal segun la sesion activa.
    @GetMapping("/")
    public String inicio(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        String destino = redirigirSegunRol(usuario);

        if (destino != null) {
            return destino;
        }

        return "redirect:/login";
    }

    // Muestra el login solo si no existe una sesion activa.
    @GetMapping("/login")
    public String mostrarLogin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        String destino = redirigirSegunRol(usuario);

        if (destino != null) {
            return destino;
        }

        return "login";
    }

    // Muestra el formulario para registrar un cliente nuevo.
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("nuevoUsuario", new Usuario());
        return "registro_cliente";
    }

    // Procesa el registro del cliente con validaciones basicas.
    @PostMapping("/registro")
    public String procesarRegistro(Usuario nuevoUsuario, Model model) {

        boolean hayErrores = false;

        if (usuarioService.existeNombreUsuario(nuevoUsuario.getNombreUsuario())) {
            model.addAttribute("errorUsuario", "Este nombre de usuario ya esta registrado.");
            hayErrores = true;
        }

        if (nuevoUsuario.getPasswordHash() == null || nuevoUsuario.getPasswordHash().length() < 6) {
            model.addAttribute("errorPassword", "La contrasena debe tener minimo 6 caracteres.");
            hayErrores = true;
        }

        if (nuevoUsuario.getTelefono() == null || nuevoUsuario.getTelefono().length() < 10) {
            model.addAttribute("errorTelefono", "El telefono debe tener minimo 10 caracteres.");
            hayErrores = true;
        }

        if (hayErrores) {
            model.addAttribute("nuevoUsuario", nuevoUsuario);
            return "registro_cliente";
        }

        usuarioService.registrarCliente(nuevoUsuario);
        return "redirect:/login";
    }

    // Procesa el inicio de sesion y redirige segun el rol.
    @PostMapping("/login")
    public String procesarLogin(Usuario usuario, Model model, HttpServletRequest request) {
        Usuario auth = usuarioService.validarLogin(
                usuario.getNombreUsuario(),
                usuario.getPasswordHash()
        );

        if (auth == null) {
            model.addAttribute("error", "Usuario o contrasena incorrectos");
            return "login";
        }

        HttpSession sesionAnterior = request.getSession(false);
        if (sesionAnterior != null) {
            sesionAnterior.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("usuarioLogueado", auth);
        session.setMaxInactiveInterval(30 * 60);

        String destino = redirigirSegunRol(auth);

        if (destino != null) {
            return destino;
        }

        model.addAttribute("error", "Rol no reconocido");
        return "login";
    }

    // Muestra la pantalla para cambiar la contrasena inicial del administrador.
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

    // Procesa el cambio de contrasena inicial del administrador.
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
            model.addAttribute("errorActual", "La contrasena actual no es correcta.");
            return "cambiar_password_inicial";
        }

        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            model.addAttribute("errorNueva", "La nueva contrasena debe tener minimo 6 caracteres.");
            return "cambiar_password_inicial";
        }

        if (!nuevaPassword.equals(confirmarPassword)) {
            model.addAttribute("errorConfirmar", "Las contrasenas no coinciden.");
            return "cambiar_password_inicial";
        }

        Usuario actualizado = usuarioService.cambiarPasswordInicial(usuario.getIdUsuario(), nuevaPassword);
        session.setAttribute("usuarioLogueado", actualizado);

        return "redirect:/admin/panel";
    }

    // Cierra la sesion del usuario y retorna al login.
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
