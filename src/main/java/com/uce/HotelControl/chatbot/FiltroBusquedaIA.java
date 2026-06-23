package com.uce.HotelControl.chatbot;

import java.time.LocalDate;

// Guarda datos detectados en la pregunta del cliente.
public class FiltroBusquedaIA {

    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;
    private String tipoHabitacion;
    private Integer cantidadPersonas;

    private boolean buscaEconomico;
    private boolean preguntaDisponibilidad;
    private boolean preguntaPrecios;
    private boolean preguntaHotel;
    private boolean preguntaFechasOcupadas;

    public FiltroBusquedaIA() {
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDate fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public String getTipoHabitacion() {
        return tipoHabitacion;
    }

    public void setTipoHabitacion(String tipoHabitacion) {
        this.tipoHabitacion = tipoHabitacion;
    }

    public Integer getCantidadPersonas() {
        return cantidadPersonas;
    }

    public void setCantidadPersonas(Integer cantidadPersonas) {
        this.cantidadPersonas = cantidadPersonas;
    }

    public boolean isBuscaEconomico() {
        return buscaEconomico;
    }

    public void setBuscaEconomico(boolean buscaEconomico) {
        this.buscaEconomico = buscaEconomico;
    }

    public boolean isPreguntaDisponibilidad() {
        return preguntaDisponibilidad;
    }

    public void setPreguntaDisponibilidad(boolean preguntaDisponibilidad) {
        this.preguntaDisponibilidad = preguntaDisponibilidad;
    }

    public boolean isPreguntaPrecios() {
        return preguntaPrecios;
    }

    public void setPreguntaPrecios(boolean preguntaPrecios) {
        this.preguntaPrecios = preguntaPrecios;
    }

    public boolean isPreguntaHotel() {
        return preguntaHotel;
    }

    public void setPreguntaHotel(boolean preguntaHotel) {
        this.preguntaHotel = preguntaHotel;
    }

    public boolean isPreguntaFechasOcupadas() {
        return preguntaFechasOcupadas;
    }

    public void setPreguntaFechasOcupadas(boolean preguntaFechasOcupadas) {
        this.preguntaFechasOcupadas = preguntaFechasOcupadas;
    }
}
