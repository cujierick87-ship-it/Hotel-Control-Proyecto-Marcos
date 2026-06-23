package com.uce.HotelControl.chatbot;

import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.InformacionHotel;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Service.HabitacionService;
import com.uce.HotelControl.Service.InformacionHotelService;
import com.uce.HotelControl.Service.ReservaService;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatbotClienteService {

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private InformacionHotelService informacionHotelService;

    @Autowired
    private GeminiChatService geminiChatService;

    // Procesa la pregunta del cliente y devuelve la respuesta final del chatbot.
    public String responderPregunta(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return "Escribe una pregunta para poder ayudarte.";
        }

        if (!esPreguntaPermitida(mensaje)) {
            return "Solo puedo ayudarte con informacion relacionada con el hotel y sus reservas";
        }

        FiltroBusquedaIA filtro = analizarMensaje(mensaje);
        String contexto = crearContextoHotel(filtro);

        return geminiChatService.generarRespuesta(mensaje.trim(), contexto);
    }

    // Evita responder temas que no tengan relacion con el hotel.
    private boolean esPreguntaPermitida(String mensaje) {
        String texto = limpiarTexto(mensaje);

        return contieneAlguna(texto,
                "hola", "buenas", "hotel", "habitacion", "habitaciones",
                "reserva", "reservas", "precio", "precios", "costo",
                "disponible", "disponibilidad", "fecha", "fechas",
                "ocupada", "ocupadas", "bloqueada", "bloqueadas",
                "servicio", "servicios", "cama", "camas", "suite",
                "sencilla", "doble", "familiar", "economico", "barato",
                "persona", "personas", "bebe", "nino", "ninos",
                "esposa", "pareja", "familia", "telefono", "direccion",
                "correo", "promocion", "promociones");
    }

    // Detecta filtros simples dentro del mensaje del cliente.
    private FiltroBusquedaIA analizarMensaje(String mensaje) {
        String texto = limpiarTexto(mensaje);
        FiltroBusquedaIA filtro = new FiltroBusquedaIA();

        if (contieneAlguna(texto, "sencilla", "simple")) {
            filtro.setTipoHabitacion("SENCILLA");
        }

        if (contieneAlguna(texto, "doble", "matrimonial", "pareja", "esposa", "esposo")) {
            filtro.setTipoHabitacion("DOBLE");
        }

        if (contieneAlguna(texto, "suite", "lujo", "amplia", "premium")) {
            filtro.setTipoHabitacion("SUITE");
        }

        filtro.setBuscaEconomico(contieneAlguna(texto,
                "economico", "barato", "menor precio", "mas barata", "mas economica"));

        filtro.setPreguntaDisponibilidad(contieneAlguna(texto,
                "disponible", "disponibilidad", "libre", "libres"));

        filtro.setPreguntaPrecios(contieneAlguna(texto,
                "precio", "precios", "costo", "cuanto", "vale"));

        filtro.setPreguntaHotel(contieneAlguna(texto,
                "hotel", "direccion", "telefono", "correo", "informacion"));

        filtro.setPreguntaFechasOcupadas(contieneAlguna(texto,
                "ocupada", "ocupadas", "bloqueada", "bloqueadas", "fechas ocupadas"));

        filtro.setCantidadPersonas(detectarCantidadPersonas(texto));
        detectarFechas(texto, filtro);

        return filtro;
    }

    // Crea el contexto real que Gemini puede usar para responder.
    private String crearContextoHotel(FiltroBusquedaIA filtro) {
        StringBuilder contexto = new StringBuilder();

        InformacionHotel info = informacionHotelService.obtenerInformacion();

        contexto.append("INFORMACION GENERAL DEL HOTEL:\n");
        contexto.append("Nombre: ").append(valor(info.getNombreHotel())).append("\n");
        contexto.append("Descripcion: ").append(valor(info.getDescripcion())).append("\n");
        contexto.append("Direccion: ").append(valor(info.getDireccion())).append("\n");
        contexto.append("Telefono: ").append(valor(info.getTelefono())).append("\n");
        contexto.append("Correo: ").append(valor(info.getCorreo())).append("\n\n");

        List<Habitacion> habitaciones = obtenerHabitacionesSegunFiltro(filtro);

        contexto.append("HABITACIONES REALES DEL SISTEMA:\n");

        if (habitaciones.isEmpty()) {
            contexto.append("No hay habitaciones disponibles con los filtros indicados.\n");
        } else {
            for (Habitacion habitacion : habitaciones) {
                contexto.append("- Habitacion ")
                        .append(valor(habitacion.getNumero()))
                        .append(" | Tipo: ")
                        .append(valor(habitacion.getTipo()))
                        .append(" | Estado: ")
                        .append(valor(habitacion.getEstado()))
                        .append(" | Precio por noche: $")
                        .append(habitacion.getPrecioNoche())
                        .append(" | Capacidad: ")
                        .append(habitacion.getCapacidad())
                        .append(" persona(s)")
                        .append(" | Camas: ")
                        .append(habitacion.getCantidadCamas())
                        .append("\n");

                contexto.append("  Descripcion: ")
                        .append(valor(habitacion.getDescripcion()))
                        .append("\n");

                contexto.append("  Servicios: ")
                        .append(valor(habitacion.getServiciosIncluidos()))
                        .append("\n");

                contexto.append("  Caracteristicas: ")
                        .append(valor(habitacion.getCaracteristicas()))
                        .append("\n");

                agregarFechasOcupadas(contexto, habitacion, filtro);
            }
        }

        contexto.append("\nREGLAS DEL SISTEMA:\n");
        contexto.append("- El chatbot solo informa y orienta.\n");
        contexto.append("- Para reservar, el cliente debe usar la interfaz de HotelControl.\n");
        contexto.append("- No se deben inventar datos que no aparezcan en este contexto.\n");

        return contexto.toString();
    }

    // Decide si se muestran todas las disponibles o las libres por fechas.
    private List<Habitacion> obtenerHabitacionesSegunFiltro(FiltroBusquedaIA filtro) {
        List<Habitacion> habitaciones;

        if (filtro.getFechaEntrada() != null
                && filtro.getFechaSalida() != null
                && filtro.getFechaSalida().isAfter(filtro.getFechaEntrada())) {
            habitaciones = habitacionService.buscarHabitacionesLibresPorFechas(
                    filtro.getFechaEntrada(),
                    filtro.getFechaSalida()
            );
        } else {
            habitaciones = habitacionService.obtenerHabitacionesDisponibles();
        }

        if (filtro.getTipoHabitacion() != null) {
            habitaciones = habitaciones.stream()
                    .filter(h -> h.getTipo() != null
                    && h.getTipo().equalsIgnoreCase(filtro.getTipoHabitacion()))
                    .toList();
        }

        if (filtro.getCantidadPersonas() != null) {
            habitaciones = habitaciones.stream()
                    .filter(h -> h.getCapacidad() != null
                    && h.getCapacidad() >= filtro.getCantidadPersonas())
                    .toList();
        }

        if (filtro.isBuscaEconomico()) {
            habitaciones = habitaciones.stream()
                    .sorted((a, b) -> Double.compare(
                    a.getPrecioNoche() != null ? a.getPrecioNoche() : 0,
                    b.getPrecioNoche() != null ? b.getPrecioNoche() : 0))
                    .toList();
        }

        return habitaciones;
    }

    // Agrega fechas ocupadas si el cliente pregunta por fechas bloqueadas.
    private void agregarFechasOcupadas(StringBuilder contexto, Habitacion habitacion, FiltroBusquedaIA filtro) {
        if (!filtro.isPreguntaFechasOcupadas()) {
            return;
        }

        List<Reserva> reservas = reservaService.buscarReservasActivasPorHabitacion(
                habitacion.getIdHabitacion()
        );

        if (reservas.isEmpty()) {
            contexto.append("  Fechas ocupadas: sin reservas activas.\n");
            return;
        }

        contexto.append("  Fechas ocupadas:\n");

        for (Reserva reserva : reservas) {
            contexto.append("  * ")
                    .append(reserva.getFechaCheckIn())
                    .append(" hasta ")
                    .append(reserva.getFechaCheckOut())
                    .append(" | Estado: ")
                    .append(reserva.getEstado())
                    .append("\n");
        }
    }

    // Detecta fechas con formato yyyy-MM-dd dentro del mensaje.
    private void detectarFechas(String texto, FiltroBusquedaIA filtro) {
        Pattern patron = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
        Matcher matcher = patron.matcher(texto);

        if (matcher.find()) {
            filtro.setFechaEntrada(LocalDate.parse(matcher.group(1)));
        }

        if (matcher.find()) {
            filtro.setFechaSalida(LocalDate.parse(matcher.group(1)));
        }
    }

    // Detecta una cantidad simple de personas.
    private Integer detectarCantidadPersonas(String texto) {
        Pattern patron = Pattern.compile("(\\d+)\\s*(persona|personas|adulto|adultos|huesped|huespedes)");
        Matcher matcher = patron.matcher(texto);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        if (contieneAlguna(texto, "mi esposa y mi bebe", "pareja y bebe")) {
            return 3;
        }

        if (contieneAlguna(texto, "mi esposa", "mi esposo", "pareja")) {
            return 2;
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
        if (texto == null) {
            return "";
        }

        String minuscula = texto.toLowerCase();
        String normalizado = Normalizer.normalize(minuscula, Normalizer.Form.NFD);

        return normalizado.replaceAll("\\p{M}", "");
    }

    private String valor(String texto) {
        if (texto == null || texto.isBlank()) {
            return "No especificado";
        }

        return texto;
    }
}