package com.uce.HotelControl.chatbot;

import com.uce.HotelControl.Model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Controlador REST que recibe preguntas del chat del cliente.
@RestController
public class ChatbotController {

    @Autowired
    private ChatbotClienteService chatbotClienteService;

    // Recibe el mensaje escrito por el cliente y devuelve la respuesta del asistente.
    @PostMapping("/cliente/chatbot/preguntar")
    public ChatbotResponse preguntar(@RequestBody ChatbotRequest request, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuario == null || !"CLIENTE".equalsIgnoreCase(usuario.getRol())) {
            return new ChatbotResponse("Debes iniciar sesión como cliente para usar el asistente.");
        }

        if (request == null || request.getMensaje() == null || request.getMensaje().trim().isEmpty()) {
            return new ChatbotResponse("Escribe una pregunta para poder ayudarte.");
        }

        String respuesta = chatbotClienteService.responderPregunta(request.getMensaje());

        return new ChatbotResponse(respuesta);
    }
}