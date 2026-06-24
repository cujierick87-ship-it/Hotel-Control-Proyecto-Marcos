package com.uce.HotelControl.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios_reserva")
public class ComentarioReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idComentario;

    @Column(nullable = false, unique = true)
    private Long reservaId;

    private String nombreCliente;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comentarioTexto;

    private String sentimiento;
    private String categoriaAfectada;
    private Boolean alertaCritica = false;
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Long getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(Long idComentario) {
        this.idComentario = idComentario;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public void setReservaId(Long reservaId) {
        this.reservaId = reservaId;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getComentarioTexto() {
        return comentarioTexto;
    }

    public void setComentarioTexto(String comentarioTexto) {
        this.comentarioTexto = comentarioTexto;
    }

    public String getSentimiento() {
        return sentimiento;
    }

    public void setSentimiento(String sentimiento) {
        this.sentimiento = sentimiento;
    }

    public String getCategoriaAfectada() {
        return categoriaAfectada;
    }

    public void setCategoriaAfectada(String categoriaAfectada) {
        this.categoriaAfectada = categoriaAfectada;
    }

    public Boolean getAlertaCritica() {
        return alertaCritica;
    }

    public void setAlertaCritica(Boolean alertaCritica) {
        this.alertaCritica = alertaCritica;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
