package com.uce.HotelControl.chatbot;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Service
public class GeminiChatService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    // Envía la pregunta y el contexto real del hotel a Gemini.
    public String generarRespuesta(String preguntaCliente, String contextoHotel) {
        try {
            String apiKey = obtenerApiKey();

            if (apiKey == null || apiKey.isBlank()) {
                return "Por el momento no puedo conectarme con el asistente inteligente. "
                        + "Aun así, puedes revisar las habitaciones disponibles en el panel principal.";
            }

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + URLEncoder.encode(geminiModel, StandardCharsets.UTF_8)
                    + ":generateContent?key="
                    + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            String cuerpo = crearCuerpoPeticion(preguntaCliente, contextoHotel);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.out.println("Chatbot Gemini respondio con error HTTP: " + response.statusCode());
                System.out.println("Detalle del error Gemini chatbot: " + response.body());
                return "No pude generar una respuesta inteligente en este momento. "
                        + "Intenta nuevamente o revisa las habitaciones disponibles.";
            }
            String respuesta = extraerTextoRespuesta(response.body());

            if (respuesta == null || respuesta.isBlank()) {
                return "No pude generar una respuesta clara en este momento.";
            }

            System.out.println("Respuesta del chatbot generada con Gemini.");
            return respuesta.trim();

        } catch (Exception e) {
            System.out.println("No se pudo usar Gemini en el chatbot. Se devolvera respuesta basica.");
            return "Tu consulta fue recibida, pero no pude conectarme con el asistente inteligente. "
                    + "Puedes consultar habitaciones, precios y disponibilidad desde el panel principal.";
        }
    }

    // Obtiene la API Key desde application.properties o desde Windows.
    private String obtenerApiKey() {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            return geminiApiKey;
        }

        return System.getenv("GOOGLE_API_KEY");
    }

    // Arma el JSON que se envía a Gemini.
    private String crearCuerpoPeticion(String preguntaCliente, String contextoHotel) throws Exception {
        ObjectNode raiz = jsonMapper.createObjectNode();

        ArrayNode contents = raiz.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");

        parts.addObject().put("text", crearPrompt(preguntaCliente, contextoHotel));

        ObjectNode generationConfig = raiz.putObject("generationConfig");
        generationConfig.put("temperature", 0.2);
        generationConfig.put("maxOutputTokens", 450);

        return jsonMapper.writeValueAsString(raiz);
    }

    // Instrucciones para que Gemini responda solo con datos reales del sistema.
    private String crearPrompt(String preguntaCliente, String contextoHotel) {
        return "Eres el asistente virtual del sistema HotelControl.\n\n"
                + "Reglas obligatorias:\n"
                + "- Responde solo sobre el hotel, habitaciones, precios, disponibilidad y reservas.\n"
                + "- Usa unicamente la informacion real entregada en el CONTEXTO DEL SISTEMA.\n"
                + "- No inventes habitaciones, precios, fechas, servicios ni promociones.\n"
                + "- No puedes crear, editar, cancelar ni confirmar reservas.\n"
                + "- Si el usuario pide hacer una reserva, indicale que debe usar el boton de reserva del sistema.\n"
                + "- Si pregunta algo fuera del hotel, responde exactamente: "
                + "\"Solo puedo ayudarte con informacion relacionada con el hotel y sus reservas\".\n"
                + "- Responde en espanol, de forma clara, amable y breve.\n\n"
                + "CONTEXTO DEL SISTEMA:\n"
                + contextoHotel + "\n\n"
                + "PREGUNTA DEL CLIENTE:\n"
                + preguntaCliente;
    }

    // Extrae el texto generado por Gemini desde el JSON de respuesta.
    private String extraerTextoRespuesta(String cuerpoRespuesta) throws Exception {
        JsonNode raiz = jsonMapper.readTree(cuerpoRespuesta);

        return raiz.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText(null);
    }
}
