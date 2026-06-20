package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.Promocion;
import com.uce.HotelControl.Repository.PromocionRepository;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PromocionService {

    @Autowired
    private PromocionRepository promocionRepository;

    // Obtiene todas las promociones para administracion.
    public List<Promocion> obtenerTodas() {
        return promocionRepository.findAll();
    }

    // Obtiene solo promociones activas para clientes.
    public List<Promocion> obtenerActivas() {
        return promocionRepository.findByEstado("ACTIVA");
    }

    // Busca una promocion por ID para editarla.
    public Promocion obtenerPorId(Long id) {
        return promocionRepository.findById(id).orElse(new Promocion());
    }

    // Guarda la promocion y conserva la imagen anterior si no se sube una nueva.
    public void guardarPromocion(Promocion promocion, MultipartFile imagenArchivo)
            throws IOException {

        if (promocion.getEstado() == null || promocion.getEstado().isEmpty()) {
            promocion.setEstado("ACTIVA");
        }

        if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
            String tipoArchivo = imagenArchivo.getContentType();
            String base64 = Base64.getEncoder().encodeToString(imagenArchivo.getBytes());
            promocion.setImagen("data:" + tipoArchivo + ";base64," + base64);
        } else {
            if (promocion.getIdPromocion() != null) {
                Promocion anterior = promocionRepository
                        .findById(promocion.getIdPromocion())
                        .orElse(null);

                if (anterior != null) {
                    promocion.setImagen(anterior.getImagen());
                }
            }
        }

        promocionRepository.save(promocion);
    }

    // Cambia el estado de una promocion entre ACTIVA e INACTIVA.
    public void cambiarEstado(Long id, String estado) {
        Promocion promocion = promocionRepository.findById(id).orElse(null);

        if (promocion != null) {
            promocion.setEstado(estado);
            promocionRepository.save(promocion);
        }
    }
}
