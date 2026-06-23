package com.uce.HotelControl.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "resenas_hotel")
public class ResenaHotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idResena;

    @OneToOne
    @JoinColumn(name = "id_reserva", nullable = false, unique = true)
    private Reserva reserva;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comentarioTexto;

    private String sentimiento;
    private String categoriaAfectada;
    private Boolean alertaCritica = false;
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public ResenaHotel() {
    }

    public Long getIdResena() {
        return idResena;
    }

    public void setIdResena(Long idResena) {
        this.idResena = idResena;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
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