
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
public interface HabitacionRepository extends JpaRepository<Habitacion, Long>{
    List<Habitacion> findByEstado(String estado);
    List<Habitacion> findByNumero(String numero);
    
}
