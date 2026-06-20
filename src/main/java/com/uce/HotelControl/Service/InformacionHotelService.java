package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.InformacionHotel;
import com.uce.HotelControl.Repository.InformacionHotelRepository;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InformacionHotelService {

    @Autowired
    private InformacionHotelRepository informacionHotelRepository;

    // Obtiene la informacion principal del hotel o crea valores por defecto.
    public InformacionHotel obtenerInformacion() {
        List<InformacionHotel> lista = informacionHotelRepository.findAll();

        if (!lista.isEmpty()) {
            return lista.get(0);
        }

        InformacionHotel info = new InformacionHotel();
        info.setNombreHotel("HotelControl");
        info.setDescripcion("Sistema de gestion hotelera.");
        info.setDireccion("Direccion del hotel");
        info.setTelefono("0999999999");
        info.setCorreo("info@hotelcontrol.com");

        return info;
    }

    // Guarda datos institucionales y conserva el logo si no se sube uno nuevo.
    public void guardarInformacion(InformacionHotel informacion, MultipartFile logoArchivo)
            throws IOException {

        List<InformacionHotel> lista = informacionHotelRepository.findAll();

        if (informacion.getIdInformacion() == null && !lista.isEmpty()) {
            informacion.setIdInformacion(lista.get(0).getIdInformacion());
        }

        if (logoArchivo != null && !logoArchivo.isEmpty()) {
            String tipoArchivo = logoArchivo.getContentType();
            String base64 = Base64.getEncoder().encodeToString(logoArchivo.getBytes());
            informacion.setLogo("data:" + tipoArchivo + ";base64," + base64);
        } else {
            if (informacion.getIdInformacion() != null) {
                InformacionHotel anterior = informacionHotelRepository
                        .findById(informacion.getIdInformacion())
                        .orElse(null);

                if (anterior != null) {
                    informacion.setLogo(anterior.getLogo());
                }
            }
        }

        informacionHotelRepository.save(informacion);
    }
}
