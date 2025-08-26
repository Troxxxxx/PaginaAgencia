package com.agencia.agencia.dto;

import java.time.LocalDateTime;

public class OrdenPagoDTO {
    private Long id;
    private String paypalOrderId;
    private Double monto;
    private String estado;
    private LocalDateTime fechaPago;

    public OrdenPagoDTO(Long id, String paypalOrderId, Double monto, String estado, LocalDateTime fechaPago) {
        this.id = id;
        this.paypalOrderId = paypalOrderId;
        this.monto = monto;
        this.estado = estado;
        this.fechaPago = fechaPago;
    }

    public Long getId() { return id; }
    public String getPaypalOrderId() { return paypalOrderId; }
    public Double getMonto() { return monto; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaPago() { return fechaPago; }
}
