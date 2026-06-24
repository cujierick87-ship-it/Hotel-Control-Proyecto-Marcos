package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.ComentarioReservaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ComentarioController {

    @Autowired
    private ComentarioReservaService comentarioReservaService;

    private Usuario obtenerClienteSesion(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null || !"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            return null;
        }

        return usuario;
    }

    // Muestra resenas publicas y el formulario cuando el cliente puede opinar.
    @GetMapping("/cliente/resenas")
    public String verResenas(Model model, HttpSession session) {
        Usuario cliente = obtenerClienteSesion(session);

        if (cliente == null) {
            return "redirect:/login?sesion=expirada";
        }

        model.addAttribute("resenas", comentarioReservaService.obtenerTodos());
        model.addAttribute("reservaElegible",
                comentarioReservaService.obtenerReservaFinalizadaSinComentario(cliente.getCedula()));

        return "resenas_publicas";
    }

    // Mantiene funcionando enlaces antiguos y los lleva al nuevo modulo.
    @GetMapping("/cliente/opiniones")
    public String redirigirOpinionesAntiguas() {
        return "redirect:/cliente/resenas";
    }

    // Guarda la resena y ejecuta Gemini primero; si falla, usa analisis local.
    @PostMapping("/cliente/resenas/guardar")
    public String guardarResena(@RequestParam Long idReserva,
            @RequestParam String comentarioTexto,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario cliente = obtenerClienteSesion(session);

        if (cliente == null) {
            return "redirect:/login?sesion=expirada";
        }

        if (comentarioTexto == null || comentarioTexto.trim().length() < 10) {
            redirectAttributes.addFlashAttribute("errorMensaje", "Escribe una resena mas detallada.");
            return "redirect:/cliente/resenas";
        }

        if (comentarioReservaService.guardarComentario(idReserva, cliente.getCedula(), comentarioTexto.trim()) == null) {
            redirectAttributes.addFlashAttribute("errorMensaje", "No se pudo registrar la resena.");
            return "redirect:/cliente/resenas";
        }

        redirectAttributes.addFlashAttribute("exitoMensaje", "Resena enviada y analizada correctamente.");
        return "redirect:/cliente/resenas";
    }
}
