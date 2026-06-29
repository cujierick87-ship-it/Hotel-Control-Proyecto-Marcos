package com.uce.HotelControl.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 *
 * @author Erick HC
 */
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReserva;

    // Datos del cliente que hace la reserva
    private String cedulaCliente;
    private String nombreCliente;

    // --- NUEVOS CAMPOS PARA EL FORMULARIO ---
    private String apellidoCliente;
    private String correo;
    private String telefono;
    private String direccion;
    private String notas;

    // ------------ATRIBUTO DE CODIGO PARA CADA RESERVA----------------------------
    private String codigoReserva;

    // Conectamos la reserva con la habitación elegida
    @ManyToOne
    @JoinColumn(name = "id_habitacion")
    private Habitacion habitacion;

    // Fechas de estadía
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaCheckIn;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaCheckOut;

    private Double totalPagar;

    // Estado: "PENDIENTE", "CONFIRMADA", "CANCELADA"
    private String estado = "PENDIENTE"; // Por defecto
    private LocalDate fechaReserva = LocalDate.now();

    // Auditoria simple para saber que acciones hizo cada recepcionista.
    private String recepcionistaReserva;
    private String recepcionistaCheckIn;
    private String recepcionistaCheckOut;
    private String recepcionistaCancelacion;
    private LocalDate fechaReservaPresencial;
    private LocalDate fechaCheckInReal;
    private LocalDate fechaCheckOutReal;
    private LocalDate fechaCancelacion;

    // Constructor vacío obligatorio para Spring Boot
    public Reserva() {
    }

    public Long getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
    }

    public String getCedulaCliente() {
        return cedulaCliente;
    }

    public void setCedulaCliente(String cedulaCliente) {
        this.cedulaCliente = cedulaCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getApellidoCliente() {
        return apellidoCliente;
    }

    public void setApellidoCliente(String apellidoCliente) {
        this.apellidoCliente = apellidoCliente;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }

    public LocalDate getFechaCheckIn() {
        return fechaCheckIn;
    }

    public void setFechaCheckIn(LocalDate fechaCheckIn) {
        this.fechaCheckIn = fechaCheckIn;
    }

    public LocalDate getFechaCheckOut() {
        return fechaCheckOut;
    }

    public void setFechaCheckOut(LocalDate fechaCheckOut) {
        this.fechaCheckOut = fechaCheckOut;
    }

    public Double getTotalPagar() {
        return totalPagar;
    }

    public void setTotalPagar(Double totalPagar) {
        this.totalPagar = totalPagar;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDate fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getCodigoReserva() {
        return codigoReserva;
    }

    public void setCodigoReserva(String codigoReserva) {
        this.codigoReserva = codigoReserva;
    }

    public String getRecepcionistaReserva() {
        return recepcionistaReserva;
    }

    public void setRecepcionistaReserva(String recepcionistaReserva) {
        this.recepcionistaReserva = recepcionistaReserva;
    }

    public String getRecepcionistaCheckIn() {
        return recepcionistaCheckIn;
    }

    public void setRecepcionistaCheckIn(String recepcionistaCheckIn) {
        this.recepcionistaCheckIn = recepcionistaCheckIn;
    }

    public String getRecepcionistaCheckOut() {
        return recepcionistaCheckOut;
    }

    public void setRecepcionistaCheckOut(String recepcionistaCheckOut) {
        this.recepcionistaCheckOut = recepcionistaCheckOut;
    }

    public String getRecepcionistaCancelacion() {
        return recepcionistaCancelacion;
    }

    public void setRecepcionistaCancelacion(String recepcionistaCancelacion) {
        this.recepcionistaCancelacion = recepcionistaCancelacion;
    }

    public LocalDate getFechaReservaPresencial() {
        return fechaReservaPresencial;
    }

    public void setFechaReservaPresencial(LocalDate fechaReservaPresencial) {
        this.fechaReservaPresencial = fechaReservaPresencial;
    }

    public LocalDate getFechaCheckInReal() {
        return fechaCheckInReal;
    }

    public void setFechaCheckInReal(LocalDate fechaCheckInReal) {
        this.fechaCheckInReal = fechaCheckInReal;
    }

    public LocalDate getFechaCheckOutReal() {
        return fechaCheckOutReal;
    }

    public void setFechaCheckOutReal(LocalDate fechaCheckOutReal) {
        this.fechaCheckOutReal = fechaCheckOutReal;
    }

    public LocalDate getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDate fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

}
