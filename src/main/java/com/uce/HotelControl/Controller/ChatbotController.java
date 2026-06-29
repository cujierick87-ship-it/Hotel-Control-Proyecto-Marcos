package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Service.ChatbotService;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Controlador REST que recibe las preguntas escritas en el chat del cliente.
@RestController
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    // Atiende el chat flotante del cliente y devuelve una respuesta en formato JSON.
    @PostMapping({"/cliente/chatbot/preguntar", "/api/chatbot/preguntar"})
    public ResponseEntity<Map<String, String>> preguntar(@RequestBody Map<String, String> body,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioCliente");

        if (usuario == null) {
            usuario = (Usuario) session.getAttribute("usuarioLogueado");
        }

        if (usuario == null || !"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            return ResponseEntity.ok(Map.of(
                    "respuesta", "Debes iniciar sesion como cliente para usar el asistente."
            ));
        }

        String mensaje = body.getOrDefault("mensaje", "");
        String respuesta = chatbotService.responder(mensaje, session);

        return ResponseEntity.ok(Map.of("respuesta", respuesta));
    }
}
