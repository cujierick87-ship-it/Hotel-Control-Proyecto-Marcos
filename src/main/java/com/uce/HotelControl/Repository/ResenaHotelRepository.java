package com.uce.HotelControl.Repository;

import com.uce.HotelControl.Model.ResenaHotel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResenaHotelRepository extends JpaRepository<ResenaHotel, Long> {

    boolean existsByReservaIdReserva(Long idReserva);

    ResenaHotel findByReservaIdReserva(Long idReserva);

    List<ResenaHotel> findAllByOrderByFechaRegistroDesc();

    long countBySentimiento(String sentimiento);

    long countByCategoriaAfectada(String categoriaAfectada);

    long countByAlertaCriticaTrue();
}