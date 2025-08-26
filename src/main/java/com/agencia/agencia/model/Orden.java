package com.agencia.agencia.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long id_orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_orden")
    private LocalDate fechaOrden;

    @Column(name = "estado_pago")
    private boolean estadoPago;

    @Column(name = "precio")
    private int precio;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleOrdenes> detalles;

    public Orden() {}

    public Long getId_orden() {
        return id_orden;
    }

    public void setId_orden(Long id_orden) {
        this.id_orden = id_orden;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFecha_orden() {
        return fechaOrden;
    }

    public void setFecha_orden(LocalDate fecha_orden) {
        this.fechaOrden = fecha_orden;
    }

    public boolean isEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(boolean estadoPago) {
        this.estadoPago = estadoPago;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public List<DetalleOrdenes> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleOrdenes> detalles) {
        this.detalles = detalles;
    }
}