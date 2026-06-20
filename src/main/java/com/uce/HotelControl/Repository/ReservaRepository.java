package com.uce.HotelControl.Repository;

import com.uce.HotelControl.Model.Reserva;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Busca reservas asociadas a la cedula de un cliente.
    List<Reserva> findByCedulaCliente(String cedulaCliente);

    // Busca una reserva por su código único.
    Reserva findByCodigoReserva(String codigoReserva);

    // Detecta reservas activas que se cruzan con un rango de fechas.
    @Query("SELECT r FROM Reserva r WHERE r.habitacion.idHabitacion = :idHabitacion "
            + "AND r.estado IN ('CONFIRMADA', 'CHECK-IN') "
            + "AND r.fechaCheckIn < :fechaFin AND r.fechaCheckOut > :fechaInicio")
    List<Reserva> encontrarChoquesDeFechas(
            @Param("idHabitacion") Long idHabitacion,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
    
    // Devuelve reservas confirmadas o en check-in de una habitacion.
    @Query("SELECT r FROM Reserva r WHERE r.habitacion.idHabitacion = :idHabitacion "
            + "AND r.estado IN ('CONFIRMADA', 'CHECK-IN')")
    List<Reserva> buscarReservasActivasPorHabitacion(@Param("idHabitacion") Long idHabitacion);
}
