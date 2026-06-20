package com.uce.HotelControl.Repository;

import com.uce.HotelControl.Model.Promocion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    // Busca promociones por estado para mostrar u ocultar al cliente.
    List<Promocion> findByEstado(String estado);
}
