///
///
///para probar git
package com.uce.HotelControl.Config;

import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Repository.UsuarioRepository;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Erick HC
 */

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UsuarioRepository usuarioRepository) {
        return args -> {
            
            // Verificamos si la tabla está vacía para no duplicar datos
            if (usuarioRepository.count() == 0) {

                // 1. Crear Administrador Maestro (Erick)
                Usuario admin = new Usuario();
                admin.setCedula("1700000000");
                admin.setNombres("Erick");
                admin.setCorreo("erick@hotelcontrol.com");
                admin.setTelefono("0999999999");
                admin.setNombreUsuario("erick_admin");
                admin.setPasswordHash("admin123"); // Clave plana (sin encoder por ahora)
                admin.setRol("ADMINISTRADOR");
                admin.setEstado("ACTIVO");
                admin.setFechaRegistro(new Date());

                usuarioRepository.save(admin);

                // 2. Crear Recepcionista de prueba (Alex)
                Usuario recepcionista = new Usuario();
                recepcionista.setCedula("1711111111");
                recepcionista.setNombres("Alex");
                recepcionista.setCorreo("recepcion@hotelcontrol.com");
                recepcionista.setTelefono("0988888888");
                recepcionista.setNombreUsuario("alex_recepcion");
                recepcionista.setPasswordHash("recepcion123");
                recepcionista.setRol("RECEPCIONISTA");
                recepcionista.setEstado("ACTIVO");
                recepcionista.setFechaRegistro(new Date());

                usuarioRepository.save(recepcionista);

                System.out.println("➔ [HotelControl] Usuarios base creados automáticamente usando Setters");
            }
        };
    }
}