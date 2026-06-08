package com.uce.HotelControl.Repository;

import com.uce.HotelControl.Model.Reserva;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Erick HC
 */
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByCedulaCliente(String cedulaCliente);

    // 🔥 LA MAGIA DE LAS FECHAS: Busca si una habitación ya está ocupada en esas fechas
    // Devuelve una lista de reservas que "chocan" con las fechas que el cliente quiere.
    @Query("SELECT r FROM Reserva r WHERE r.habitacion.idHabitacion = :idHabitacion "
            + "AND r.estado IN ('CONFIRMADA', 'CHECK-IN') "
            + "AND r.fechaCheckIn < :fechaFin AND r.fechaCheckOut > :fechaInicio")
    List<Reserva> encontrarChoquesDeFechas(
            @Param("idHabitacion") Long idHabitacion,
            @Param("fechaInicio") Date fechaInicio,
            @Param("fechaFin") Date fechaFin);

}
