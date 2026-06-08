
package com.uce.HotelControl.Repository;

import com.uce.HotelControl.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Erick HC
 */

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
    
    // Este método buscará automáticamente al usuario por su nombre de usuario
    Usuario findByNombreUsuario(String nombreUsuario);
    
}
