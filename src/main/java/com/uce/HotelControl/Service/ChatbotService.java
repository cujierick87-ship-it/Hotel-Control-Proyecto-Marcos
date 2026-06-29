package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.ComentarioReserva;
import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.InformacionHotel;
import com.uce.HotelControl.Model.Promocion;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Repository.ComentarioReservaRepository;
import com.uce.HotelControl.Repository.HabitacionRepository;
import com.uce.HotelControl.Repository.PromocionRepository;
import com.uce.HotelControl.Repository.ReservaRepository;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

// Servicio del chatbot. Consulta datos reales del sistema y pide a Gemini redactar la respuesta.
@Service
public class ChatbotService {

    private static final String RESPUESTA_FUERA_DE_CONTEXTO =
            "Solo puedo ayudarte con informacion relacionada con el hotel y sus reservas.";

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Autowired
    private InformacionHotelService informacionHotelService;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private PromocionRepository promocionRepository;

    @Autowired
    private ComentarioReservaRepository comentarioReservaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    // Valida la pregunta, arma el contexto real y solicita la respuesta a Gemini.
    public String responder(String mensajeUsuario, HttpSession session) {
        if (mensajeUsuario == null || mensajeUsuario.trim().isEmpty()) {
            return "Por favor escribe tu pregunta.";
        }

        String mensajeLimpio = limpiarTexto(mensajeUsuario);
        String tema = detectarTema(mensajeLimpio, session);
        guardarFechasEnMemoria(mensajeUsuario, session);

        if ("FUERA_DE_CONTEXTO".equals(tema)) {
            return RESPUESTA_FUERA_DE_CONTEXTO;
        }

        guardarTemaEnMemoria(tema, session);

        try {
            String apiKey = obtenerApiKey();

            if (apiKey == null || apiKey.isBlank()) {
                return "No se encontro una API Key de Gemini. Revisa gemini.api.key o GOOGLE_API_KEY.";
            }

            String contexto = construirContextoHotel(tema, session, mensajeUsuario);
            String prompt = construirPrompt(mensajeUsuario.trim(), contexto, tema);
            String cuerpo = crearCuerpoPeticion(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(crearUrlGemini(apiKey)))
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
                return "No pude generar una respuesta inteligente en este momento. Intenta nuevamente.";
            }

            String respuesta = extraerTextoRespuesta(response.body());

            if (respuesta == null || respuesta.isBlank()) {
                return "No pude generar una respuesta clara en este momento.";
            }

            System.out.println("Respuesta del chatbot generada con Gemini.");
            return respuesta.trim();

        } catch (Exception e) {
            System.out.println("No se pudo usar Gemini en el chatbot: " + e.getMessage());
            return "No pude conectarme con el asistente inteligente en este momento.";
        }
    }

    // Detecta si la consulta pertenece al hotel y clasifica su tema principal.
    private String detectarTema(String texto, HttpSession session) {
        if (contieneAlguna(texto, "hola", "buenas", "buenos dias", "buenas tardes", "buenas noches")) {
            return "SALUDO";
        }

        if (contieneAlguna(texto, "gracias", "adios", "hasta luego", "nos vemos", "chao")) {
            return "DESPEDIDA";
        }

        if (contieneAlguna(texto, "resena", "resenas", "opinion", "opiniones", "comentario", "comentarios")) {
            return "RESENAS";
        }

        if (contieneAlguna(texto, "wifi", "desayuno", "mascota", "mascotas", "servicio", "servicios",
                "limpieza", "parqueadero", "internet", "comida", "recepcion", "atencion")) {
            return "SERVICIOS";
        }

        if (contieneAlguna(texto, "recomienda", "recomiendas", "viajo", "familia", "pareja",
                "bebe", "nino", "ninos", "comoda", "descansar")) {
            return "RECOMENDACIONES";
        }

        if (contieneAlguna(texto, "precio", "precios", "costo", "barata", "barato",
                "cara", "caro", "economica", "economico")) {
            return "PRECIOS";
        }

        if (contieneAlguna(texto, "disponible", "disponibilidad", "libre", "ocupada",
                "ocupadas", "bloqueada", "bloqueadas", "fecha", "fechas")) {
            return "DISPONIBILIDAD";
        }

        if (contieneAlguna(texto, "reserva", "reservas", "check in", "check-in",
                "check out", "check-out", "comprobante", "cancelar", "presencial")) {
            return "RESERVAS";
        }

        if (contieneAlguna(texto, "hotel", "ubicado", "ubicacion", "direccion",
                "telefono", "correo", "contacto", "promocion", "promociones", "horario",
                "24 horas", "24h", "recepcion")) {
            return "INFORMACION_GENERAL";
        }

        if (contieneAlguna(texto, "habitacion", "habitaciones", "suite", "doble",
                "sencilla", "cama", "camas", "capacidad")) {
            return "HABITACIONES";
        }

        if (esPreguntaDeSeguimiento(texto, session)) {
            return obtenerUltimoTema(session);
        }

        return "FUERA_DE_CONTEXTO";
    }

    // Guarda el ultimo tema para entender preguntas como "y la mas barata".
    private void guardarTemaEnMemoria(String tema, HttpSession session) {
        if (session == null) {
            return;
        }

        if (!"SALUDO".equals(tema) && !"DESPEDIDA".equals(tema) && !"FUERA_DE_CONTEXTO".equals(tema)) {
            session.setAttribute("chatbotUltimoTema", tema);
        }
    }

    // Reutiliza el tema anterior cuando el cliente hace una pregunta corta.
    private boolean esPreguntaDeSeguimiento(String texto, HttpSession session) {
        if (session == null || obtenerUltimoTema(session) == null) {
            return false;
        }

        return contieneAlguna(texto, "y la mas barata", "y la mas cara", "la mas barata",
                "la mas cara", "otra opcion", "tambien", "y esa", "y ese", "cual");
    }

    private String obtenerUltimoTema(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object tema = session.getAttribute("chatbotUltimoTema");
        return tema != null ? tema.toString() : null;
    }

    // Construye el contexto con datos reales de la base de datos.
    private String construirContextoHotel(String tema, HttpSession session, String mensajeUsuario) {
        StringBuilder contexto = new StringBuilder();
        InformacionHotel info = obtenerInformacionHotel();

        contexto.append("INFORMACION GENERAL DEL HOTEL:\n");
        contexto.append("Nombre: ").append(valor(info.getNombreHotel(), "HotelControl")).append("\n");
        contexto.append("Descripcion: ").append(valor(info.getDescripcion(), "No registrada")).append("\n");
        contexto.append("Direccion: ").append(valor(info.getDireccion(), "No registrada")).append("\n");
        contexto.append("Telefono: ").append(valor(info.getTelefono(), "No registrado")).append("\n");
        contexto.append("Correo: ").append(valor(info.getCorreo(), "No registrado")).append("\n");
        contexto.append("Mision: ").append(valor(info.getMision(), "No registrada")).append("\n");
        contexto.append("Vision: ").append(valor(info.getVision(), "No registrada")).append("\n");
        contexto.append("Horario de check-in: 14:00 horas.\n");
        contexto.append("Horario de check-out: 11:00 horas.\n");
        contexto.append("Horario de recepcion: consultar directamente con recepcion.\n");
        contexto.append("Mascotas: consultar disponibilidad y condiciones en recepcion.\n\n");

        contexto.append("DATOS DE CONTACTO QUE DEBEN RESPONDERSE SI EL CLIENTE PREGUNTA:\n");
        contexto.append("- Direccion oficial: ").append(valor(info.getDireccion(), "No registrada")).append("\n");
        contexto.append("- Telefono oficial: ").append(valor(info.getTelefono(), "No registrado")).append("\n");
        contexto.append("- Correo oficial: ").append(valor(info.getCorreo(), "No registrado")).append("\n\n");

        agregarFuncionesDelSistema(contexto);
        agregarDisponibilidadConsultada(contexto, mensajeUsuario, session);
        agregarHabitaciones(contexto);
        agregarHabitacionesDestacadas(contexto);
        agregarPromociones(contexto);
        agregarResenas(contexto);
        agregarReservasActivas(contexto);

        contexto.append("\nCONTEXTO DE CONVERSACION:\n");
        contexto.append("Tema detectado ahora: ").append(tema).append("\n");
        contexto.append("Tema anterior: ").append(valor(obtenerUltimoTema(session), "Ninguno")).append("\n");

        return contexto.toString();
    }

    // Explica a Gemini que el usuario pregunta por acciones del sistema, no por acciones del chat.
    private void agregarFuncionesDelSistema(StringBuilder contexto) {
        contexto.append("FUNCIONES REALES QUE PUEDE USAR EL CLIENTE EN HOTELCONTROL:\n");
        contexto.append("- Consultar habitaciones disponibles desde la pestana Habitaciones.\n");
        contexto.append("- Ver detalles de una habitacion, precio, capacidad, servicios y calendario de fechas bloqueadas.\n");
        contexto.append("- Crear una reserva web seleccionando fechas de entrada, salida y notas adicionales.\n");
        contexto.append("- Ver sus reservas desde Mis reservas.\n");
        contexto.append("- Ver el comprobante de una reserva para presentarlo en recepcion.\n");
        contexto.append("- Cancelar una reserva desde Mis reservas solo si todavia esta en estado CONFIRMADA.\n");
        contexto.append("- Ver y actualizar su perfil.\n");
        contexto.append("- Ver informacion del hotel, promociones y opiniones de huespedes.\n");
        contexto.append("- Dejar una resena cuando tenga una reserva FINALIZADA y aun no haya dejado resena para esa reserva.\n");
        contexto.append("- La resena se analiza con Gemini si esta disponible y con analisis local como respaldo.\n\n");

        contexto.append("FUNCIONES OPERATIVAS DEL HOTEL:\n");
        contexto.append("- El recepcionista puede registrar check-in, check-out, cancelar reservas y registrar reservas presenciales.\n");
        contexto.append("- Una reserva presencial es para clientes que llegan directamente al hotel y la registra recepcion.\n");
        contexto.append("- El administrador gestiona habitaciones, usuarios, reportes, informacion institucional, promociones y resenas.\n\n");

        contexto.append("ACLARACION PARA RESPONDER PREGUNTAS CON 'PUEDO':\n");
        contexto.append("- Si el cliente pregunta 'puedo reservar', 'puedo cancelar' o 'puedo dejar resena', responde si el USUARIO puede hacerlo en el sistema.\n");
        contexto.append("- No respondas como si preguntara si el chatbot puede hacerlo. El chatbot solo orienta, pero el usuario si puede usar las funciones disponibles.\n\n");
    }

    private InformacionHotel obtenerInformacionHotel() {
        return informacionHotelService.obtenerInformacion();
    }

    // Agrega habitaciones reales con capacidad, precio, servicios y estado.
    private void agregarHabitaciones(StringBuilder contexto) {
        List<Habitacion> habitaciones = habitacionRepository.findAllByOrderByNumeroAsc();

        contexto.append("HABITACIONES REALES DEL SISTEMA:\n");

        if (habitaciones.isEmpty()) {
            contexto.append("No hay habitaciones registradas actualmente.\n\n");
            return;
        }

        for (Habitacion habitacion : habitaciones) {
            contexto.append("- Habitacion ").append(valor(habitacion.getNumero(), "N/A"))
                    .append(" | Tipo: ").append(valor(habitacion.getTipo(), "N/A"))
                    .append(" | Estado: ").append(valor(habitacion.getEstado(), "N/A"))
                    .append(" | Precio por noche: $").append(valorNumero(habitacion.getPrecioNoche()))
                    .append(" | Capacidad: ").append(valorNumero(habitacion.getCapacidad())).append(" persona(s)")
                    .append(" | Camas: ").append(valorNumero(habitacion.getCantidadCamas()))
                    .append("\n");

            contexto.append("  Descripcion: ").append(valor(habitacion.getDescripcion(), "No registrada")).append("\n");
            contexto.append("  Servicios: ").append(valor(habitacion.getServiciosIncluidos(), "No registrados")).append("\n");
            contexto.append("  Caracteristicas: ").append(valor(habitacion.getCaracteristicas(), "No registradas")).append("\n");
        }

        contexto.append("\n");
    }

    // Calcula habitaciones libres cuando el cliente escribe una fecha concreta.
    private void agregarDisponibilidadConsultada(StringBuilder contexto, String mensajeUsuario, HttpSession session) {
        List<LocalDate> fechas = detectarFechas(mensajeUsuario);

        if (fechas.isEmpty()) {
            LocalDate fechaGuardada = obtenerFechaGuardada(session);

            if (fechaGuardada != null) {
                fechas.add(fechaGuardada);
            }
        }

        if (fechas.isEmpty()) {
            return;
        }

        LocalDate fechaInicio = fechas.get(0);
        LocalDate fechaFin = fechas.size() > 1 ? fechas.get(1) : fechas.get(0).plusDays(1);

        if (!fechaFin.isAfter(fechaInicio)) {
            fechaFin = fechaInicio.plusDays(1);
        }

        final LocalDate inicioConsulta = fechaInicio;
        final LocalDate finConsulta = fechaFin;

        List<Habitacion> habitaciones = habitacionRepository.findAllByOrderByNumeroAsc();
        List<Habitacion> disponibles = habitaciones.stream()
                .filter(h -> habitacionDisponibleEnRango(h, inicioConsulta, finConsulta))
                .toList();

        contexto.append("DISPONIBILIDAD CONSULTADA POR EL CLIENTE:\n");
        contexto.append("Fecha de entrada consultada: ").append(fechaInicio).append("\n");
        contexto.append("Fecha de salida usada para validar: ").append(fechaFin).append("\n");

        if (disponibles.isEmpty()) {
            contexto.append("No hay habitaciones disponibles para ese rango.\n\n");
            return;
        }

        contexto.append("Habitaciones disponibles para ese rango:\n");

        for (Habitacion habitacion : disponibles) {
            contexto.append("- Habitacion ")
                    .append(valor(habitacion.getNumero(), "N/A"))
                    .append(": ")
                    .append(valor(habitacion.getTipo(), "N/A"))
                    .append(", $")
                    .append(valorNumero(habitacion.getPrecioNoche()))
                    .append(" por noche, capacidad ")
                    .append(valorNumero(habitacion.getCapacidad()))
                    .append(" persona(s).\n");
        }

        contexto.append("\n");
    }

    private boolean habitacionDisponibleEnRango(Habitacion habitacion, LocalDate fechaInicio, LocalDate fechaFin) {
        if (habitacion == null || "MANTENIMIENTO".equalsIgnoreCase(habitacion.getEstado())) {
            return false;
        }

        List<Reserva> reservas = reservaRepository.encontrarChoquesDeFechas(
                habitacion.getIdHabitacion(),
                fechaInicio,
                fechaFin
        );

        return reservas.isEmpty();
    }

    // Agrega resumen de habitacion economica y costosa para preguntas de precio.
    private void agregarHabitacionesDestacadas(StringBuilder contexto) {
        List<Habitacion> habitaciones = habitacionRepository.findAllByOrderByNumeroAsc()
                .stream()
                .filter(h -> h.getPrecioNoche() != null)
                .toList();

        if (habitaciones.isEmpty()) {
            return;
        }

        Habitacion economica = habitaciones.stream()
                .min(Comparator.comparing(Habitacion::getPrecioNoche))
                .orElse(null);

        Habitacion cara = habitaciones.stream()
                .max(Comparator.comparing(Habitacion::getPrecioNoche))
                .orElse(null);

        contexto.append("RESUMEN DE PRECIOS:\n");

        if (economica != null) {
            contexto.append("- Habitacion mas economica: ")
                    .append(economica.getNumero())
                    .append(" | $").append(economica.getPrecioNoche())
                    .append(" | Tipo: ").append(valor(economica.getTipo(), "N/A"))
                    .append(" | Capacidad: ").append(valorNumero(economica.getCapacidad()))
                    .append("\n");
        }

        if (cara != null) {
            contexto.append("- Habitacion mas cara: ")
                    .append(cara.getNumero())
                    .append(" | $").append(cara.getPrecioNoche())
                    .append(" | Tipo: ").append(valor(cara.getTipo(), "N/A"))
                    .append(" | Capacidad: ").append(valorNumero(cara.getCapacidad()))
                    .append("\n");
        }

        contexto.append("\n");
    }

    // Agrega promociones activas registradas por el administrador.
    private void agregarPromociones(StringBuilder contexto) {
        List<Promocion> promociones = promocionRepository.findByEstado("ACTIVA");

        contexto.append("PROMOCIONES ACTIVAS:\n");

        if (promociones.isEmpty()) {
            contexto.append("No hay promociones activas en este momento.\n\n");
            return;
        }

        for (Promocion promocion : promociones) {
            contexto.append("- ")
                    .append(valor(promocion.getTitulo(), "Promocion"))
                    .append(": ")
                    .append(valor(promocion.getDescripcion(), "Sin descripcion"))
                    .append("\n");
        }

        contexto.append("\n");
    }

    // Agrega algunas opiniones registradas para responder sobre resenas.
    private void agregarResenas(StringBuilder contexto) {
        List<ComentarioReserva> resenas = comentarioReservaRepository.findAllByOrderByFechaRegistroDesc();

        contexto.append("RESENAS Y OPINIONES DE HUESPEDES:\n");

        if (resenas.isEmpty()) {
            contexto.append("Aun no hay resenas registradas.\n\n");
            return;
        }

        int limite = Math.min(5, resenas.size());

        for (int i = 0; i < limite; i++) {
            ComentarioReserva resena = resenas.get(i);
            contexto.append("- ")
                    .append(valor(resena.getNombreCliente(), "Huesped"))
                    .append(" | Sentimiento: ")
                    .append(valor(resena.getSentimiento(), "No analizado"))
                    .append(" | Comentario: ")
                    .append(valor(resena.getComentarioTexto(), "Sin comentario"))
                    .append("\n");
        }

        contexto.append("\n");
    }

    // Agrega reservas activas para explicar fechas ocupadas cuando el cliente pregunte.
    private void agregarReservasActivas(StringBuilder contexto) {
        List<Reserva> reservas = reservaRepository.findAll()
                .stream()
                .filter(r -> "CONFIRMADA".equalsIgnoreCase(r.getEstado())
                || "CHECK-IN".equalsIgnoreCase(r.getEstado()))
                .toList();

        contexto.append("FECHAS OCUPADAS POR RESERVAS ACTIVAS:\n");

        if (reservas.isEmpty()) {
            contexto.append("No hay reservas activas registradas.\n\n");
            return;
        }

        for (Reserva reserva : reservas) {
            if (reserva.getHabitacion() != null) {
                contexto.append("- Habitacion ")
                        .append(valor(reserva.getHabitacion().getNumero(), "N/A"))
                        .append(": ")
                        .append(reserva.getFechaCheckIn())
                        .append(" hasta ")
                        .append(reserva.getFechaCheckOut())
                        .append(" | Estado: ")
                        .append(valor(reserva.getEstado(), "N/A"))
                        .append("\n");
            }
        }

        contexto.append("\n");
    }

    // Prompt principal enviado a Gemini con reglas de seguridad y dominio.
    private String construirPrompt(String mensajeUsuario, String contexto, String tema) {
        return "Eres el asistente virtual de HotelControl.\n\n"
                + "TEMA DETECTADO: " + tema + "\n\n"
                + "REGLAS OBLIGATORIAS:\n"
                + "- Responde solo sobre el hotel y el sistema HotelControl.\n"
                + "- Temas permitidos: saludos, despedidas, habitaciones, reservas, disponibilidad, precios, "
                + "resenas, opiniones, servicios, politicas, horarios, promociones, informacion general y recomendaciones.\n"
                + "- Usa solo los datos del CONTEXTO DEL SISTEMA.\n"
                + "- Si el cliente pregunta por direccion, telefono o correo, responde con el dato exacto del bloque DATOS DE CONTACTO.\n"
                + "- No inventes habitaciones, precios, fechas, promociones, servicios ni politicas.\n"
                + "- Diferencia entre el chatbot y el usuario: el chatbot no ejecuta acciones, pero el usuario si puede usar las funciones del sistema.\n"
                + "- Si preguntan 'puedo reservar', responde que el cliente puede hacerlo desde Habitaciones o detalles de habitacion, seleccionando fechas.\n"
                + "- Si preguntan 'puedo cancelar', responde que puede cancelar desde Mis reservas cuando la reserva esta CONFIRMADA y no ha iniciado check-in.\n"
                + "- Si preguntan 'puedo hacer reserva presencial', explica que esa funcion la registra recepcion para clientes que llegan directamente al hotel.\n"
                + "- Si preguntan 'como dejo una resena', explica que debe entrar a Resenas y solo se habilita cuando tiene una reserva FINALIZADA sin resena previa.\n"
                + "- Si recomiendas una habitacion, explica por que usando precio, capacidad, servicios o comodidad.\n"
                + "- No termines respuestas con frases incompletas.\n"
                + "- Si el dato exacto no esta registrado, dilo claramente y sugiere consultar con recepcion.\n"
                + "- No uses Markdown: no escribas **, tablas ni numeracion compleja.\n"
                + "- Si enumeras habitaciones, usa lineas separadas con guion. Maximo 5 habitaciones por respuesta.\n"
                + "- En preguntas de disponibilidad, usa primero el bloque DISPONIBILIDAD CONSULTADA POR EL CLIENTE si existe.\n"
                + "- Si preguntan algo fuera del hotel, responde exactamente: \"" + RESPUESTA_FUERA_DE_CONTEXTO + "\"\n"
                + "- Responde en espanol, con tono amable, claro y breve.\n\n"
                + "CONTEXTO DEL SISTEMA:\n"
                + contexto + "\n\n"
                + "PREGUNTA DEL CLIENTE:\n"
                + mensajeUsuario;
    }

    private String crearUrlGemini(String apiKey) {
        return "https://generativelanguage.googleapis.com/v1beta/models/"
                + URLEncoder.encode(geminiModel, StandardCharsets.UTF_8)
                + ":generateContent?key="
                + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
    }

    // Arma el JSON de la peticion a Gemini usando Jackson 3.
    private String crearCuerpoPeticion(String prompt) throws Exception {
        ObjectNode raiz = jsonMapper.createObjectNode();

        ArrayNode contents = raiz.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        ObjectNode generationConfig = raiz.putObject("generationConfig");
        generationConfig.put("temperature", 0.2);
        generationConfig.put("maxOutputTokens", 700);

        return jsonMapper.writeValueAsString(raiz);
    }

    // Extrae el texto de la respuesta JSON generada por Gemini.
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

    private String obtenerApiKey() {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            return geminiApiKey;
        }

        return System.getenv("GOOGLE_API_KEY");
    }

    private void guardarFechasEnMemoria(String mensajeUsuario, HttpSession session) {
        if (session == null) {
            return;
        }

        List<LocalDate> fechas = detectarFechas(mensajeUsuario);

        if (!fechas.isEmpty()) {
            session.setAttribute("chatbotUltimaFecha", fechas.get(0).toString());
        }
    }

    private LocalDate obtenerFechaGuardada(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object valor = session.getAttribute("chatbotUltimaFecha");

        if (valor == null) {
            return null;
        }

        try {
            return LocalDate.parse(valor.toString());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private List<LocalDate> detectarFechas(String textoOriginal) {
        List<LocalDate> fechas = new ArrayList<>();

        if (textoOriginal == null || textoOriginal.isBlank()) {
            return fechas;
        }

        java.util.regex.Pattern patron = java.util.regex.Pattern.compile(
                "(\\d{1,2}/\\d{1,2}/\\d{4}|\\d{4}-\\d{1,2}-\\d{1,2})"
        );
        java.util.regex.Matcher matcher = patron.matcher(textoOriginal);

        while (matcher.find()) {
            LocalDate fecha = convertirFecha(matcher.group(1));

            if (fecha != null) {
                fechas.add(fecha);
            }
        }

        return fechas;
    }

    private LocalDate convertirFecha(String textoFecha) {
        String[] patrones = {"d/M/yyyy", "dd/MM/yyyy", "yyyy-M-d", "yyyy-MM-dd"};

        for (String patron : patrones) {
            try {
                return LocalDate.parse(textoFecha, DateTimeFormatter.ofPattern(patron));
            } catch (DateTimeParseException e) {
                // Intenta con el siguiente formato permitido.
            }
        }

        return null;
    }

    private boolean contieneAlguna(String texto, String... palabras) {
        for (String palabra : palabras) {
            if (texto.contains(palabra)) {
                return true;
            }
        }

        return false;
    }

    private String limpiarTexto(String texto) {
        String minuscula = texto == null ? "" : texto.toLowerCase();
        String normalizado = Normalizer.normalize(minuscula, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{M}", "");
    }

    private String valor(String texto, String defecto) {
        if (texto == null || texto.isBlank()) {
            return defecto;
        }

        return texto;
    }

    private String valorNumero(Number numero) {
        if (numero == null) {
            return "No especificado";
        }

        return numero.toString();
    }
}
