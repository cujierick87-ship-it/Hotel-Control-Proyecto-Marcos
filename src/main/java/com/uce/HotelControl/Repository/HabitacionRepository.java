package com.uce.HotelControl.Repository;

import com.uce.HotelControl.Model.Habitacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Erick HC
 */
@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {
    // Devuelve habitaciones ordenadas por numero como apoyo a los listados.
    List<Habitacion> findAllByOrderByNumeroAsc();

    // Busca habitaciones por estado: DISPONIBLE, OCUPADA, LIMPIEZA o MANTENIMIENTO.

    List<Habitacion> findByEstado(String estado);

    // Busca habitaciones por estado y mantiene un primer orden por numero.
    List<Habitacion> findByEstadoOrderByNumeroAsc(String estado);

    // Busca habitaciones por número.
    List<Habitacion> findByNumero(String numero);

}
