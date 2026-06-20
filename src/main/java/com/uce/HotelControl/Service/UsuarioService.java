package com.uce.HotelControl.Service;

import com.uce.HotelControl.Model.Usuario;
import com.uce.HotelControl.Repository.UsuarioRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Valida el inicio de sesión.
    // Busca el usuario por nombre de usuario, compara la contraseña y verifica que esté ACTIVO.
    public Usuario validarLogin(String nombreUsuario, String password) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (usuario != null) {
            if (usuario.getPasswordHash().equals(password) && usuario.getEstado().equals("ACTIVO")) {
                return usuario;
            }
        }

        return null;
    }

    // Registra un nuevo cliente.
    // Asigna automáticamente rol CLIENTE, estado ACTIVO y fecha de registro.
    public void registrarCliente(Usuario nuevoCliente) {
        nuevoCliente.setRol("CLIENTE");
        nuevoCliente.setEstado("ACTIVO");
        nuevoCliente.setFechaRegistro(new java.util.Date());
        nuevoCliente.setRequiereCambioPassword(false);

        usuarioRepository.save(nuevoCliente);
    }

    // Verifica si ya existe un usuario con ese nombre de usuario.
    // Se usa para evitar registros duplicados.
    public boolean existeNombreUsuario(String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario);

        if (usuario != null) {
            return true;
        }

        return false;
    }

    // Cambia la contraseña inicial del administrador.
    // Después del cambio, desactiva la obligación de cambiar contraseña.
    public Usuario cambiarPasswordInicial(Long idUsuario, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);

        if (usuario != null) {
            usuario.setPasswordHash(nuevaPassword);
            usuario.setRequiereCambioPassword(false);
            usuarioRepository.save(usuario);
        }

        return usuario;
    }

    // Obtiene todos los usuarios registrados.
    // Se usa en el panel administrativo para gestionar personal.
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    // Guarda o actualiza un usuario.
    // Si no tiene estado, fecha o valor de cambio de contraseña, se asignan valores por defecto.
    public void guardarUsuario(Usuario usuario) {
        if (usuario.getEstado() == null) {
            usuario.setEstado("ACTIVO");
        }

        if (usuario.getFechaRegistro() == null) {
            usuario.setFechaRegistro(new java.util.Date());
        }

        if (usuario.getRequiereCambioPassword() == null) {
            usuario.setRequiereCambioPassword(false);
        }

        usuarioRepository.save(usuario);
    }

    // Busca un usuario por su ID.
    // Si no existe, devuelve un usuario vacío para evitar errores en formularios.
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(new Usuario());
    }

    // Elimina un usuario por su ID.
    // Se usa para la gestión simple de personal desde administración.
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    // Actualiza los datos personales del cliente.
    // No cambia el nombre de usuario para evitar problemas con usuarios repetidos.
    public Usuario actualizarPerfilCliente(Long idUsuario, Usuario datosPerfil) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);

        if (usuario != null) {
            usuario.setNombres(datosPerfil.getNombres());
            usuario.setApellidos(datosPerfil.getApellidos());
            usuario.setCedula(datosPerfil.getCedula());
            usuario.setTelefono(datosPerfil.getTelefono());
            usuario.setCorreo(datosPerfil.getCorreo());

            usuarioRepository.save(usuario);
        }

        return usuario;
    }
}
