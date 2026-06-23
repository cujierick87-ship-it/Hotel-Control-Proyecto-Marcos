package com.uce.HotelControl.Service;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AnalisisSentimientoService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public ResultadoAnalisisSentimiento analizarComentario(String comentario) {
        ResultadoAnalisisSentimiento resultadoGemini = analizarConGemini(comentario);

        if (resultadoGemini != null) {
            System.out.println("Analisis de resena realizado con Gemini.");
            return resultadoGemini;
        }

        System.out.println("Analisis de resena realizado localmente.");
        return analizarLocalmente(comentario);
    }

    private ResultadoAnalisisSentimiento analizarConGemini(String comentario) {
        try {
            String apiKey = obtenerApiKey();

            if (apiKey == null || apiKey.isBlank()) {
                System.out.println("Gemini no se ejecuto: falta configurar GOOGLE_API_KEY.");
                return null;
            }

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + URLEncoder.encode(geminiModel, StandardCharsets.UTF_8)
                    + ":generateContent?key="
                    + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            String cuerpo = crearCuerpoPeticion(comentario);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
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
                System.out.println("Gemini respondio con error HTTP: " + response.statusCode());
                return null;
            }

            String textoRespuesta = extraerTextoRespuesta(response.body());

            if (textoRespuesta == null || textoRespuesta.isBlank()) {
                System.out.println("Gemini respondio sin texto para analizar.");
                return null;
            }

            String jsonLimpio = limpiarJson(textoRespuesta);

            ResultadoAnalisisSentimiento resultado = objectMapper.readValue(
                    jsonLimpio,
                    ResultadoAnalisisSentimiento.class
            );

            return validarResultado(resultado);

        } catch (Exception e) {
            System.out.println("Gemini no pudo analizar la resena. Se usara analisis local.");
            return null;
        }
    }

    private String obtenerApiKey() {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            return geminiApiKey;
        }

        return System.getenv("GOOGLE_API_KEY");
    }

    private String crearCuerpoPeticion(String comentario) throws Exception {
        ObjectNode raiz = objectMapper.createObjectNode();

        ArrayNode contents = raiz.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");

        parts.addObject().put("text", crearPrompt(comentario));

        ObjectNode generationConfig = raiz.putObject("generationConfig");
        generationConfig.put("temperature", 0.1);
        generationConfig.put("responseMimeType", "application/json");

        return objectMapper.writeValueAsString(raiz);
    }

    private String crearPrompt(String comentario) {
        return "Analiza la siguiente resena de un huesped sobre un hotel.\n\n"
                + "Responde unicamente con JSON valido.\n"
                + "No agregues explicaciones.\n"
                + "No uses markdown.\n"
                + "No uses texto antes ni despues del JSON.\n\n"
                + "Formato obligatorio:\n"
                + "{\n"
                + "  \"sentimiento\": \"POSITIVO\",\n"
                + "  \"categoriaAfectada\": \"GENERAL\",\n"
                + "  \"alertaCritica\": false\n"
                + "}\n\n"
                + "Valores permitidos para sentimiento:\n"
                + "POSITIVO, NEGATIVO, NEUTRO\n\n"
                + "Valores permitidos para categoriaAfectada:\n"
                + "LIMPIEZA, ATENCION, INFRAESTRUCTURA, COMODIDAD, GENERAL\n\n"
                + "Reglas:\n"
                + "- POSITIVO si predomina una buena experiencia.\n"
                + "- NEGATIVO si predominan quejas o problemas.\n"
                + "- NEUTRO si no hay una opinion clara.\n"
                + "- alertaCritica debe ser true solo si hay peligro, robo, violencia, accidente, "
                + "insalubridad grave, plagas, gas, incendio o una urgencia importante.\n"
                + "- Si no hay categoria clara, usa GENERAL.\n\n"
                + "Resena del huesped:\n"
                + comentario;
    }

    private String extraerTextoRespuesta(String cuerpoRespuesta) throws Exception {
        JsonNode raiz = objectMapper.readTree(cuerpoRespuesta);

        return raiz.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText(null);
    }

    private String limpiarJson(String respuesta) {
        if (respuesta == null) {
            return "{}";
        }

        String texto = respuesta.trim();
        texto = texto.replace("```json", "");
        texto = texto.replace("```", "");
        texto = texto.trim();

        int inicio = texto.indexOf("{");
        int fin = texto.lastIndexOf("}");

        if (inicio >= 0 && fin >= inicio) {
            return texto.substring(inicio, fin + 1);
        }

        return texto;
    }

    private ResultadoAnalisisSentimiento validarResultado(ResultadoAnalisisSentimiento resultado) {
        if (resultado == null) {
            return null;
        }

        resultado.setSentimiento(normalizarValor(resultado.getSentimiento()));
        resultado.setCategoriaAfectada(normalizarValor(resultado.getCategoriaAfectada()));

        if (!esSentimientoValido(resultado.getSentimiento())) {
            resultado.setSentimiento("NEUTRO");
        }

        if (!esCategoriaValida(resultado.getCategoriaAfectada())) {
            resultado.setCategoriaAfectada("GENERAL");
        }

        if (resultado.getAlertaCritica() == null) {
            resultado.setAlertaCritica(false);
        }

        return resultado;
    }

    private String normalizarValor(String valor) {
        if (valor == null) {
            return "";
        }

        return limpiarTexto(valor).toUpperCase().trim();
    }

    private boolean esSentimientoValido(String valor) {
        return "POSITIVO".equals(valor)
                || "NEGATIVO".equals(valor)
                || "NEUTRO".equals(valor);
    }

    private boolean esCategoriaValida(String valor) {
        return "LIMPIEZA".equals(valor)
                || "ATENCION".equals(valor)
                || "INFRAESTRUCTURA".equals(valor)
                || "COMODIDAD".equals(valor)
                || "GENERAL".equals(valor);
    }

    private ResultadoAnalisisSentimiento analizarLocalmente(String comentario) {
        String texto = limpiarTexto(comentario);

        int positivos = contarCoincidencias(texto,
                "excelente", "bueno", "buena", "limpio", "limpia",
                "amable", "comodo", "comoda", "rapido", "rapida",
                "agradable", "recomendado", "bonito", "tranquilo",
                "perfecto", "feliz");

        int negativos = contarCoincidencias(texto,
                "malo", "mala", "sucio", "sucia", "ruido",
                "demora", "lento", "lenta", "problema", "pesimo",
                "incomodo", "incomoda", "danado", "danada", "mal",
                "terrible", "horrible");

        String sentimiento = "NEUTRO";

        if (positivos > negativos) {
            sentimiento = "POSITIVO";
        }

        if (negativos > positivos) {
            sentimiento = "NEGATIVO";
        }

        return new ResultadoAnalisisSentimiento(
                sentimiento,
                detectarCategoria(texto),
                detectarAlertaCritica(texto)
        );
    }

    private String detectarCategoria(String texto) {
        if (contieneAlguna(texto, "sucio", "sucia", "limpieza", "olor", "bano",
                "sabana", "toalla", "polvo", "mancha")) {
            return "LIMPIEZA";
        }

        if (contieneAlguna(texto, "recepcion", "personal", "trato", "atencion",
                "demora", "amable", "servicio", "empleado")) {
            return "ATENCION";
        }

        if (contieneAlguna(texto, "wifi", "internet", "ducha", "agua", "aire",
                "television", "puerta", "luz", "ascensor")) {
            return "INFRAESTRUCTURA";
        }

        if (contieneAlguna(texto, "cama", "colchon", "almohada", "descanso",
                "ruido", "incomodo", "comodo", "dormir")) {
            return "COMODIDAD";
        }

        return "GENERAL";
    }

    private Boolean detectarAlertaCritica(String texto) {
        return contieneAlguna(texto, "robo", "incendio", "gas", "accidente",
                "inundacion", "plaga", "chinches", "violencia", "peligro",
                "emergencia", "inseguro", "amenaza", "salud");
    }

    private int contarCoincidencias(String texto, String... palabras) {
        int contador = 0;

        for (String palabra : palabras) {
            if (texto.contains(palabra)) {
                contador++;
            }
        }

        return contador;
    }

    private Boolean contieneAlguna(String texto, String... palabras) {
        for (String palabra : palabras) {
            if (texto.contains(palabra)) {
                return true;
            }
        }

        return false;
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return "";
        }

        String minuscula = texto.toLowerCase();
        String normalizado = Normalizer.normalize(minuscula, Normalizer.Form.NFD);

        return normalizado.replaceAll("\\p{M}", "");
    }
}
