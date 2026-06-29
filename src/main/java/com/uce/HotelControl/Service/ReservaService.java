package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Repository.HabitacionRepository;
import com.uce.HotelControl.Repository.ReservaRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {

    // DTO simple para mostrar estadisticas por recepcionista en el dashboard.
    public static class ResumenRecepcionista {

        private String nombre;
        private int reservasCreadas;
        private int checkIns;
        private int checkOuts;
        private int cancelaciones;

        public ResumenRecepcionista(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }

        public int getReservasCreadas() {
            return reservasCreadas;
        }

        public int getCheckIns() {
            return checkIns;
        }

        public int getCheckOuts() {
            return checkOuts;
        }

        public int getCancelaciones() {
            return cancelaciones;
        }

        public void sumarReserva() {
            reservasCreadas++;
        }

        public void sumarCheckIn() {
            checkIns++;
        }

        public void sumarCheckOut() {
            checkOuts++;
        }

        public void sumarCancelacion() {
            cancelaciones++;
        }
    }

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

// Guarda una reserva hecha por el cliente.
// Calcula el total, genera código único y registra la reserva en estado CONFIRMADA.
    public Reserva guardarReserva(Reserva reserva) {
        Habitacion habitacion = habitacionRepository
                .findById(reserva.getHabitacion().getIdHabitacion())
                .orElse(null);

        if (habitacion == null) {
            return null;
        }

        if ("MANTENIMIENTO".equalsIgnoreCase(habitacion.getEstado())) {
            return null;
        }

        if (reserva.getFechaCheckIn() == null || reserva.getFechaCheckOut() == null) {
            return null;
        }

        if (!reserva.getFechaCheckOut().isAfter(reserva.getFechaCheckIn())) {
            return null;
        }

        List<Reserva> choques = reservaRepository.encontrarChoquesDeFechas(
                habitacion.getIdHabitacion(),
                reserva.getFechaCheckIn(),
                reserva.getFechaCheckOut()
        );

        if (!choques.isEmpty()) {
            return null;
        }

        long dias = ChronoUnit.DAYS.between(
                reserva.getFechaCheckIn(),
                reserva.getFechaCheckOut()
        );

        if (dias <= 0) {
            dias = 1;
        }

        reserva.setTotalPagar(dias * habitacion.getPrecioNoche());

        reserva.setEstado("CONFIRMADA");

        reserva.setHabitacion(habitacion);

        if (reserva.getCodigoReserva() == null || reserva.getCodigoReserva().isEmpty()) {
            String codigo = "HC-" + java.util.UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();

            reserva.setCodigoReserva(codigo);
        }

        return reservaRepository.save(reserva);
    }

    // Procesa acciones operativas de recepcion sobre una reserva.
    public void procesarAccionRecepcion(Long idReserva, String accion) {
        procesarAccionRecepcion(idReserva, accion, null);
    }

    // Procesa acciones operativas y guarda quien las realizo para los reportes.
    public void procesarAccionRecepcion(Long idReserva, String accion, String nombreRecepcionista) {
        Reserva reserva = reservaRepository.findById(idReserva).orElse(null);

        if (reserva != null) {
            Habitacion habitacion = reserva.getHabitacion();

            if (accion.equals("CHECKIN")) {
                reserva.setEstado("CHECK-IN");
                if (habitacion != null) {
                    habitacion.setEstado("OCUPADA");
                }
                reserva.setRecepcionistaCheckIn(nombreRecepcionista);
                reserva.setFechaCheckInReal(LocalDate.now());
            }

            if (accion.equals("CHECKOUT")) {
                reserva.setEstado("FINALIZADA");
                if (habitacion != null) {
                    habitacion.setEstado("LIMPIEZA");
                }
                reserva.setRecepcionistaCheckOut(nombreRecepcionista);
                reserva.setFechaCheckOutReal(LocalDate.now());
            }

            if (accion.equals("CANCELAR")) {
                reserva.setEstado("CANCELADA");
                reserva.setRecepcionistaCancelacion(nombreRecepcionista);
                reserva.setFechaCancelacion(LocalDate.now());
            }

            if (accion.equals("NOSHOW")) {
                reserva.setEstado("NO-SHOW");

                if (habitacion != null) {
                    habitacion.setEstado("DISPONIBLE");
                }
            }

            reservaRepository.save(reserva);

            if (habitacion != null) {
                habitacionRepository.save(habitacion);
            }
        }
    }

    // Devuelve todas las reservas registradas.
    public List<Reserva> obtenerTodasLasReservas() {
        return reservaRepository.findAll();
    }

    // Busca reservas por cedula del cliente.
    public List<Reserva> buscarPorCedula(String cedula) {
        return reservaRepository.findByCedulaCliente(cedula);
    }

    // Busca una reserva por ID.
    // Se usa para mostrar comprobante y cancelar reservas.
    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id).orElse(null);
    }

    // Cancela una reserva del cliente solo si aun esta confirmada.
    public boolean cancelarReservaCliente(Long idReserva, String cedulaCliente) {
        Reserva reserva = reservaRepository.findById(idReserva).orElse(null);

        if (reserva == null) {
            return false;
        }

        if (!reserva.getCedulaCliente().equals(cedulaCliente)) {
            return false;
        }

        if (!"CONFIRMADA".equalsIgnoreCase(reserva.getEstado())) {
            return false;
        }

        reserva.setEstado("CANCELADA");
        reservaRepository.save(reserva);
        return true;
    }

    // Busca reservas por codigo unico o por cedula.
    public List<Reserva> buscarPorCodigoOCedula(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return reservaRepository.findAll();
        }

        Reserva porCodigo = reservaRepository.findByCodigoReserva(filtro.trim());

        if (porCodigo != null) {
            return java.util.Collections.singletonList(porCodigo);
        }

        return reservaRepository.findByCedulaCliente(filtro.trim());
    }

    // Obtiene reservas activas de una habitacion para bloquear fechas.
    public List<Reserva> buscarReservasActivasPorHabitacion(Long idHabitacion) {
        return reservaRepository.buscarReservasActivasPorHabitacion(idHabitacion);
    }

    // Registra una reserva presencial reutilizando las validaciones generales.
    public Reserva registrarReservaPresencial(Reserva reserva, Long idHabitacion) {
        return registrarReservaPresencial(reserva, idHabitacion, null);
    }

    // Registra una reserva presencial e identifica al recepcionista que la creo.
    public Reserva registrarReservaPresencial(Reserva reserva, Long idHabitacion, String nombreRecepcionista) {
        Habitacion habitacion = habitacionRepository.findById(idHabitacion).orElse(null);

        if (habitacion == null) {
            return null;
        }

        reserva.setHabitacion(habitacion);
        reserva.setRecepcionistaReserva(nombreRecepcionista);
        reserva.setFechaReservaPresencial(LocalDate.now());

        return guardarReserva(reserva);
    }

    // Suma ingresos sin contar reservas canceladas ni no-show.
    public double calcularIngresosValidos(List<Reserva> reservas) {
        double total = 0;

        for (Reserva reserva : reservas) {
            if (reserva.getTotalPagar() != null
                    && !"CANCELADA".equalsIgnoreCase(reserva.getEstado())
                    && !"NO-SHOW".equalsIgnoreCase(reserva.getEstado())) {
                total = total + reserva.getTotalPagar();
            }
        }

        return total;
    }

    // Cuenta reservas segun su estado.
    public int contarReservasPorEstado(String estado) {
        int contador = 0;

        for (Reserva reserva : reservaRepository.findAll()) {
            if (estado.equalsIgnoreCase(reserva.getEstado())) {
                contador++;
            }
        }

        return contador;
    }

    // Filtra reservas por fecha de entrada dentro de un rango.
    public List<Reserva> buscarReservasPorRango(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Reserva> resultado = new ArrayList<>();

        for (Reserva reserva : reservaRepository.findAll()) {
            if (reserva.getFechaCheckIn() != null
                    && !reserva.getFechaCheckIn().isBefore(fechaInicio)
                    && !reserva.getFechaCheckIn().isAfter(fechaFin)) {
                resultado.add(reserva);
            }
        }

        return resultado;
    }

    // Genera un conteo simple de habitaciones mas reservadas.
    public Map<String, Integer> obtenerHabitacionesMasReservadas() {
        Map<String, Integer> conteo = new LinkedHashMap<>();

        for (Reserva reserva : reservaRepository.findAll()) {
            if (reserva.getHabitacion() != null) {
                String numero = reserva.getHabitacion().getNumero();

                if (!conteo.containsKey(numero)) {
                    conteo.put(numero, 1);
                } else {
                    conteo.put(numero, conteo.get(numero) + 1);
                }
            }
        }

        return conteo;
    }

    // Lista clientes unicos usando la primera reserva encontrada por cedula.
    public List<Reserva> obtenerClientesConReservas() {
        List<Reserva> clientes = new ArrayList<>();
        List<String> cedulas = new ArrayList<>();

        for (Reserva reserva : reservaRepository.findAll()) {
            if (reserva.getCedulaCliente() != null && !cedulas.contains(reserva.getCedulaCliente())) {
                cedulas.add(reserva.getCedulaCliente());
                clientes.add(reserva);
            }
        }

        return clientes;
    }

    // Cuenta las acciones realizadas hoy por un recepcionista especifico.
    public Map<String, Integer> obtenerResumenHoyRecepcionista(String nombreRecepcionista) {
        Map<String, Integer> resumen = new LinkedHashMap<>();
        LocalDate hoy = LocalDate.now();

        resumen.put("reservasCreadas", 0);
        resumen.put("checkIns", 0);
        resumen.put("checkOuts", 0);
        resumen.put("cancelaciones", 0);

        for (Reserva reserva : reservaRepository.findAll()) {
            if (mismoRecepcionista(nombreRecepcionista, reserva.getRecepcionistaReserva())
                    && hoy.equals(reserva.getFechaReservaPresencial())) {
                resumen.put("reservasCreadas", resumen.get("reservasCreadas") + 1);
            }

            if (mismoRecepcionista(nombreRecepcionista, reserva.getRecepcionistaCheckIn())
                    && hoy.equals(reserva.getFechaCheckInReal())) {
                resumen.put("checkIns", resumen.get("checkIns") + 1);
            }

            if (mismoRecepcionista(nombreRecepcionista, reserva.getRecepcionistaCheckOut())
                    && hoy.equals(reserva.getFechaCheckOutReal())) {
                resumen.put("checkOuts", resumen.get("checkOuts") + 1);
            }

            if (mismoRecepcionista(nombreRecepcionista, reserva.getRecepcionistaCancelacion())
                    && hoy.equals(reserva.getFechaCancelacion())) {
                resumen.put("cancelaciones", resumen.get("cancelaciones") + 1);
            }
        }

        return resumen;
    }

    // Agrupa reservas y acciones por recepcionista para el dashboard administrativo.
    public List<ResumenRecepcionista> obtenerResumenPorRecepcionista() {
        Map<String, ResumenRecepcionista> resumen = new LinkedHashMap<>();

        for (Reserva reserva : reservaRepository.findAll()) {
            sumarRecepcionista(resumen, reserva.getRecepcionistaReserva(), "RESERVA");
            sumarRecepcionista(resumen, reserva.getRecepcionistaCheckIn(), "CHECKIN");
            sumarRecepcionista(resumen, reserva.getRecepcionistaCheckOut(), "CHECKOUT");
            sumarRecepcionista(resumen, reserva.getRecepcionistaCancelacion(), "CANCELACION");
        }

        return new ArrayList<>(resumen.values());
    }

    // Evita repetir codigo al contar acciones por recepcionista.
    private void sumarRecepcionista(Map<String, ResumenRecepcionista> resumen,
            String nombreRecepcionista, String accion) {

        if (nombreRecepcionista == null || nombreRecepcionista.isBlank()) {
            return;
        }

        ResumenRecepcionista item = resumen.computeIfAbsent(
                nombreRecepcionista,
                ResumenRecepcionista::new
        );

        if ("RESERVA".equals(accion)) {
            item.sumarReserva();
        }

        if ("CHECKIN".equals(accion)) {
            item.sumarCheckIn();
        }

        if ("CHECKOUT".equals(accion)) {
            item.sumarCheckOut();
        }

        if ("CANCELACION".equals(accion)) {
            item.sumarCancelacion();
        }
    }

    // Compara nombres tolerando valores vacios.
    private boolean mismoRecepcionista(String esperado, String actual) {
        if (esperado == null || actual == null) {
            return false;
        }

        return esperado.trim().equalsIgnoreCase(actual.trim());
    }

}
