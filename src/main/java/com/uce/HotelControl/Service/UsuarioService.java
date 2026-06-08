package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Repository.UsuarioRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Erick HC
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método para validar las credenciales
    public Usuario validarLogin(String nombreUsuario, String password) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (usuario != null) {
            // Compara la contraseña y verifica que el estado sea ACTIVO
            if (usuario.getPasswordHash().equals(password) && usuario.getEstado().equals("ACTIVO")) {
                return usuario; // Credenciales correctas
            }
        }
        return null; // Credenciales incorrectas o usuario inactivo
    }

    // Método NUEVO: Guarda al cliente en la base de datos
    public void registrarCliente(Usuario nuevoCliente) {
        // Le asignamos por defecto los valores obligatorios para un cliente nuevo
        nuevoCliente.setRol("CLIENTE");
        nuevoCliente.setEstado("ACTIVO");
        nuevoCliente.setFechaRegistro(new java.util.Date());

        // Guardamos en PostgreSQL usando el repositorio
        usuarioRepository.save(nuevoCliente);
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    public void guardarUsuario(Usuario usuario) {
        // Asegurarnos de que tenga estado y fecha al crearse
        if (usuario.getEstado() == null) {
            usuario.setEstado("ACTIVO");
        }
        if (usuario.getFechaRegistro() == null) {
            usuario.setFechaRegistro(new java.util.Date());
        }
        usuarioRepository.save(usuario);
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(new Usuario());
    }

   
    // Para cumplir con "Inhabilitar cuenta" de tu diagrama de forma sencilla
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}
