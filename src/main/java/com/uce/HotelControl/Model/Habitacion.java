package com.uce.HotelControl.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 *
 * @author Erick HC
 */
@Entity
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHabitacion;

    private String numero;
    private String tipo;
    private Integer capacidad;
    private Double precioNoche;
    private String descripcion;

    
    @Column(columnDefinition = "TEXT")
    private String imagen;

    private String estado;

    public Habitacion() {
    }

    public Habitacion(String numero, String tipo, Integer capacidad,
            Double precioNoche, String descripcion, String estado) {
        this.numero = numero;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.precioNoche = precioNoche;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public Long getIdHabitacion() {
        return idHabitacion;
    }

    public void setIdHabitacion(Long idHabitacion) {
        this.idHabitacion = idHabitacion;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public Double getPrecioNoche() {
        return precioNoche;
    }

    public void setPrecioNoche(Double precioNoche) {
        this.precioNoche = precioNoche;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}