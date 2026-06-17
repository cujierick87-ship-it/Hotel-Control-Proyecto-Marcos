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
    // Busca habitaciones por estado: DISPONIBLE, OCUPADA, LIMPIEZA o MANTENIMIENTO.

    List<Habitacion> findByEstado(String estado);

    // Busca habitaciones por número.
    List<Habitacion> findByNumero(String numero);

}
