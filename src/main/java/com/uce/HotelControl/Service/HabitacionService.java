package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.Habitacion;
import com.uce.HotelControl.Model.Reserva;
import com.uce.HotelControl.Repository.HabitacionRepository;
import com.uce.HotelControl.Repository.ReservaRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class HabitacionService {

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    // Busca habitaciones libres en un rango de fechas.
    // Descarta habitaciones en mantenimiento y revisa cruces con reservas existentes.
    public List<Habitacion> buscarHabitacionesLibresPorFechas(Date checkIn, Date checkOut) {
        List<Habitacion> todas = habitacionRepository.findAll();
        List<Habitacion> disponibles = new ArrayList<>();

        for (Habitacion hab : todas) {
            if ("MANTENIMIENTO".equals(hab.getEstado()) || hab.getEstado() == null) {
                continue;
            }
            List<Reserva> choques = reservaRepository.encontrarChoquesDeFechas(
                    hab.getIdHabitacion(),
                    checkIn,
                    checkOut
            );
            if (choques.isEmpty()) {
                disponibles.add(hab);
            }
        }   
        return disponibles;
    }
    
    // Obtiene habitaciones con estado DISPONIBLE.
    // Se usa para mostrar habitaciones disponibles en el catálogo del cliente.
    public List<Habitacion> obtenerHabitacionesDisponibles() {
        return habitacionRepository.findByEstado("DISPONIBLE");
    }

    // Guarda o actualiza una habitación sin procesar imagen.
    // Se deja por si otro proceso del sistema necesita guardar habitación sin archivo.
    public void guardarHabitacion(Habitacion habitacion) {
        if (habitacion.getEstado() == null || habitacion.getEstado().isEmpty()) {
            habitacion.setEstado("DISPONIBLE");
        }

        habitacionRepository.save(habitacion);
    }

    // Guarda o actualiza una habitación con imagen subida desde la PC.
    // Si se sube imagen, la convierte a Base64 y la guarda en la base de datos.
    // Si se edita y no se sube nueva imagen, conserva la imagen anterior.
    public void guardarHabitacion(Habitacion habitacion, MultipartFile imagenArchivo)
            throws IOException {

        if (habitacion.getEstado() == null || habitacion.getEstado().isEmpty()) {
            habitacion.setEstado("DISPONIBLE");
        }

        if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
            String tipoArchivo = imagenArchivo.getContentType();
            String base64 = Base64.getEncoder().encodeToString(imagenArchivo.getBytes());

            habitacion.setImagen("data:" + tipoArchivo + ";base64," + base64);
        } else {
            if (habitacion.getIdHabitacion() != null) {
                Habitacion anterior = habitacionRepository
                        .findById(habitacion.getIdHabitacion())
                        .orElse(null);

                if (anterior != null) {
                    habitacion.setImagen(anterior.getImagen());
                }
            }
        }

        habitacionRepository.save(habitacion);
    }

    // Obtiene todas las habitaciones registradas.
    // Se usa en el panel administrador y en recepción.
    public List<Habitacion> obtenerTodasLasHabitaciones() {
        return habitacionRepository.findAll();
    }

    // Busca habitaciones por número.
    // Se usa en el buscador del administrador.
    public List<Habitacion> buscarPorNumero(String numero) {
        return habitacionRepository.findByNumero(numero);
    }

    // Busca una habitación por ID.
    // Se usa para cargar datos al formulario de edición.
    public Habitacion obtenerPorId(Long id) {
        return habitacionRepository.findById(id).orElse(new Habitacion());
    }

    // Da de baja una habitación.
    // No la elimina, solo cambia su estado a MANTENIMIENTO.
    public void darDeBaja(Long id) {
        Habitacion hab = habitacionRepository.findById(id).orElse(null);

        if (hab != null) {
            hab.setEstado("MANTENIMIENTO");
            habitacionRepository.save(hab);
        }
    }

    // Actualiza el estado operativo de una habitación.
    // Permite cambiar entre DISPONIBLE, OCUPADA, LIMPIEZA y MANTENIMIENTO.
    public void actualizarEstado(Long idHabitacion, String estado) {
        Habitacion habitacion = habitacionRepository.findById(idHabitacion).orElse(null);

        if (habitacion != null) {
            habitacion.setEstado(estado);
            habitacionRepository.save(habitacion);
        }
    }

    // Cambia una habitación de LIMPIEZA a DISPONIBLE.
    // Se usa desde recepción cuando termina la limpieza.
    public void marcarComoLimpia(Long idHabitacion) {
        Habitacion habitacion = habitacionRepository.findById(idHabitacion).orElse(null);

        if (habitacion != null) {
            habitacion.setEstado("DISPONIBLE");
            habitacionRepository.save(habitacion);
        }
    }
}