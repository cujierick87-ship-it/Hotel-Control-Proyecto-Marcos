package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.ComentarioReservaService;
import com.uce.HotelControl.Service.HabitacionService;
import com.uce.HotelControl.Service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private ComentarioReservaService comentarioReservaService;

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

    // Define la llave de sesion que corresponde a cada rol.
    private String atributoPorRol(String rol) {
        if ("ADMINISTRADOR".equalsIgnoreCase(rol)) {
            return "usuarioAdmin";
        }

        if ("RECEPCIONISTA".equalsIgnoreCase(rol)) {
            return "usuarioRecepcionista";
        }

        if ("CLIENTE".equalsIgnoreCase(rol)) {
            return "usuarioCliente";
        }

        return "usuarioLogueado";
    }

    // Guarda el usuario por rol para permitir varias sesiones en el mismo navegador.
    private void guardarUsuarioPorRol(HttpSession session, Usuario usuario) {
        session.setAttribute(atributoPorRol(usuario.getRol()), usuario);
        session.setAttribute("usuarioLogueado", usuario);
    }

    // Redirige la ruta principal segun la sesion activa.
    @GetMapping("/")
    public String inicio(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioAdmin");
        if (usuario == null) {
            usuario = (Usuario) session.getAttribute("usuarioRecepcionista");
        }
        if (usuario == null) {
            usuario = (Usuario) session.getAttribute("usuarioCliente");
        }

        String destino = redirigirSegunRol(usuario);

        if (destino != null) {
            return destino;
        }

        // La portada solo muestra una seleccion breve de los datos existentes.
        model.addAttribute("habitacionesDestacadas",
                habitacionService.obtenerHabitacionesDisponibles().stream().limit(3).toList());
        model.addAttribute("ultimasResenas",
                comentarioReservaService.obtenerTodos().stream().limit(3).toList());

        return "index";
    }

    // Muestra el login aunque exista otro rol activo para permitir sesiones paralelas.
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

        HttpSession session = request.getSession(true);
        guardarUsuarioPorRol(session, auth);
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
        Usuario usuario = (Usuario) session.getAttribute("usuarioAdmin");

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

        Usuario usuario = (Usuario) session.getAttribute("usuarioAdmin");

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
        session.setAttribute("usuarioAdmin", actualizado);
        session.setAttribute("usuarioLogueado", actualizado);

        return "redirect:/admin/panel";
    }

    // Cierra solo el rol indicado; si no llega rol, cierra toda la sesion.
    @GetMapping("/logout")
    public String logout(HttpSession session, @RequestParam(required = false) String rol) {
        if (rol == null || rol.isBlank()) {
            session.invalidate();
            return "redirect:/login";
        }

        session.removeAttribute(atributoPorRol(rol));

        // Borra el contexto corto del chatbot cuando sale el cliente.
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            session.removeAttribute("chatbotUltimoTema");
            session.removeAttribute("chatbotUltimaFecha");
        }

        Usuario actual = (Usuario) session.getAttribute("usuarioLogueado");
        if (actual != null && actual.getRol().equalsIgnoreCase(rol)) {
            session.removeAttribute("usuarioLogueado");
        }

        return "redirect:/login";
    }
}
