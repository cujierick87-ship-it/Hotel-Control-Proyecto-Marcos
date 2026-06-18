package com.uce.HotelControl.Repository;

import com.uce.HotelControl.Model.SolicitudRecepcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudRecepcionRepository extends JpaRepository<SolicitudRecepcion, Long> {
}
