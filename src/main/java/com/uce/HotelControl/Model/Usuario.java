package com.uce.HotelControl.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

/**
 *
 * @author Erick HC
 */
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    private String cedula;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String nombreUsuario;
    private String passwordHash;
    private String rol;
    private String estado;
    private Boolean requiereCambioPassword = false;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;

    public Usuario() {
    }

    public Usuario(String cedula, String nombres, String apellidos, String correo, String telefono,
            String nombreUsuario, String passwordHash, String rol, String estado, Date fechaRegistro) {
        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.telefono = telefono;
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Boolean getRequiereCambioPassword() {
        return requiereCambioPassword;
    }

    public void setRequiereCambioPassword(Boolean requiereCambioPassword) {
        this.requiereCambioPassword = requiereCambioPassword;
    }
}
