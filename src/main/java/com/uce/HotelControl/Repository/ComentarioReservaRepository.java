package com.uce.HotelControl.Repository;

import com.uce.HotelControl.Model.ComentarioReserva;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioReservaRepository extends JpaRepository<ComentarioReserva, Long> {

    boolean existsByReservaId(Long reservaId);

    List<ComentarioReserva> findAllByOrderByFechaRegistroDesc();

    long countBySentimiento(String sentimiento);

    long countByCategoriaAfectada(String categoriaAfectada);

    long countByAlertaCriticaTrue();
}