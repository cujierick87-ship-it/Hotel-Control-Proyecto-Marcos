package com.uce.HotelControl.Service;

public class ResultadoAnalisisSentimiento {

    private String sentimiento;
    private String categoriaAfectada;
    private Boolean alertaCritica;

    public ResultadoAnalisisSentimiento() {
    }

    public ResultadoAnalisisSentimiento(String sentimiento, String categoriaAfectada, Boolean alertaCritica) {
        this.sentimiento = sentimiento;
        this.categoriaAfectada = categoriaAfectada;
        this.alertaCritica = alertaCritica;
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
}