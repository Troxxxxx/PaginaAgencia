package com.agencia.agencia.dto;

public class FavoritoDTO {

    private Long idCarro;
    private String marca;
    private String modelo;
    private Integer ano;
    private Integer precio;
    private String imagenUrl; // Nuevo campo para la imagen

    public FavoritoDTO(Long idCarro, String marca, String modelo, Integer ano, Integer precio, String imagenUrl) {
        this.idCarro = idCarro;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
    }

    // Getters y setters existentes
    public Long getIdCarro() {
        return idCarro;
    }

    public void setIdCarro(Long idCarro) {
        this.idCarro = idCarro;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getPrecio() {
        return precio;
    }

    public void setPrecio(Integer precio) {
        this.precio = precio;
    }

    // Getter y setter para imagenUrl
    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}