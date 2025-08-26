package com.agencia.agencia.controller;

import com.agencia.agencia.dto.OrdenPagoDTO;
import com.agencia.agencia.model.Orden;
import com.agencia.agencia.model.OrdenPago;
import com.agencia.agencia.service.OrdenPagoService;
import com.agencia.agencia.service.OrdenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/factura")
public class OrdenPagoController {

    private final OrdenPagoService ordenPagoService;
    private final OrdenService ordenService;

    public OrdenPagoController(OrdenPagoService ordenPagoService, OrdenService ordenService) {
        this.ordenPagoService = ordenPagoService;
        this.ordenService = ordenService;
    }

    @GetMapping("/by-id/{id}")
public ResponseEntity<?> obtenerFacturaPorId(@PathVariable Long id) {
    return ordenPagoService.buscarPorId(id)
            .<ResponseEntity<?>>map(pago -> ResponseEntity.ok(new OrdenPagoDTO(
                    pago.getId(),
                    pago.getPaypalOrderId(),
                    pago.getMonto(),
                    pago.getEstado(),
                    pago.getFechaPago()
            )))
            .orElseGet(() -> ResponseEntity.status(404).body("Factura no encontrada"));
}

    @GetMapping("/by-orden/{ordenId}")
    public ResponseEntity<?> obtenerFacturaPorOrden(@PathVariable Long ordenId) {
        Orden orden = ordenService.findById(ordenId);
        if (orden == null) {
            return ResponseEntity.status(404).body("Orden no encontrada");
        }

        Optional<OrdenPago> pago = ordenPagoService.buscarPorOrden(orden);

        if (pago.isPresent()) {
            OrdenPago p = pago.get();
            return ResponseEntity.ok(new OrdenPagoDTO(
                    p.getId(),
                    p.getPaypalOrderId(),
                    p.getMonto(),
                    p.getEstado(),
                    p.getFechaPago()
            ));
        } else {
            return ResponseEntity.status(404).body("Factura no encontrada");
        }
    }
    
}        
