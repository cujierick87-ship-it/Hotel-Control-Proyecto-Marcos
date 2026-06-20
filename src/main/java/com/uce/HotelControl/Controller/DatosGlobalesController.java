package com.uce.HotelControl.Controller;

import com.uce.HotelControl.Model.InformacionHotel;
import com.uce.HotelControl.Service.InformacionHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class DatosGlobalesController {

    @Autowired
    private InformacionHotelService informacionHotelService;

    // Carga el logo y nombre del hotel en todas las vistas.
    @ModelAttribute("hotelGlobal")
    public InformacionHotel cargarInformacionHotel() {
        return informacionHotelService.obtenerInformacion();
    }
}
