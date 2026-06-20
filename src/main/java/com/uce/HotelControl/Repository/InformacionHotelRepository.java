package com.uce.HotelControl.Repository;

import com.uce.HotelControl.Model.InformacionHotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InformacionHotelRepository extends JpaRepository<InformacionHotel, Long> {
}