package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Repository.HabitacionRepository;
import com.uce.HotelControl.Repository.ReservaRepository; // 🔥 NUEVO IMPORT
import java.util.ArrayList; // 🔥 NUEVO IMPORT
import java.util.Date; // 🔥 NUEVO IMPORT
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Erick HC
 */
@Service
public class HabitacionService {

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private ReservaRepository reservaRepository; // 🔥 NUEVO: Necesario para revisar cruces de fechas

    // =========================================================================
    // 🚀 NUEVO MOTOR DE DISPONIBILIDAD (Solución al Bloqueo Absoluto de Fechas)
    // =========================================================================
    public List<Habitacion> buscarHabitacionesLibresPorFechas(Date checkIn, Date checkOut) {
        List<Habitacion> todas = habitacionRepository.findAll();
        List<Habitacion> disponibles = new ArrayList<>();

        for (Habitacion hab : todas) {
            // Descartamos las habitaciones que el Admin dio de baja por daños
            if ("MANTENIMIENTO".equals(hab.getEstado()) || hab.getEstado() == null) {
                continue;
            }
            // Consultamos al repositorio si existen reservas que colisionen en esas fechas
            List<Reserva> choques = reservaRepository.encontrarChoquesDeFechas(hab.getIdHabitacion(), checkIn, checkOut);

            // Si la lista de colisiones está vacía, la habitación está libre para ese rango de días
            if (choques.isEmpty()) {
                disponibles.add(hab);
            }
        }
        return disponibles;
    }

    // =========================================================================
    // SUS MÉTODOS ANTERIORES (CONSERVADOS AL 100%)
    // =========================================================================
    // Método para el RF-02: Catálogo del Cliente
    public List<Habitacion> obtenerHabitacionesDisponibles() {
        // Le pedimos al repositorio SOLO las que tengan estado "DISPONIBLE"
        return habitacionRepository.findByEstado("DISPONIBLE");
    }

    // Método extra: Nos servirá más adelante cuando el Administrador quiera crear cuartos nuevos
    public void guardarHabitacion(Habitacion habitacion) {
        habitacionRepository.save(habitacion);
    }

    // Método NUEVO para el Admin: Ver TODO el inventario sin filtros
    public List<Habitacion> obtenerTodasLasHabitaciones() {
        return habitacionRepository.findAll();
    }

    public List<Habitacion> buscarPorNumero(String numero) {
        return habitacionRepository.findByNumero(numero);
    }

    // NUEVO: Buscar una sola por ID (Nos servirá para cargar el formulario al editar)
    public Habitacion obtenerPorId(Long id) {
        return habitacionRepository.findById(id).orElse(new Habitacion());
    }

    // 3. Dar de baja (Cambiar estado a MANTENIMIENTO)
    public void darDeBaja(Long id) {
        Habitacion hab = habitacionRepository.findById(id).orElse(null);
        if (hab != null) {
            hab.setEstado("MANTENIMIENTO");
            habitacionRepository.save(hab);
        }
    }

    // Para el CU-04: Actualizar de Limpieza a Disponible
    public void marcarComoLimpia(Long idHabitacion) {
        Habitacion habitacion = habitacionRepository.findById(idHabitacion).orElse(null);
        if (habitacion != null) {
            habitacion.setEstado("DISPONIBLE");
            habitacionRepository.save(habitacion);
        }
    }
}
