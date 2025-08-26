package com.agencia.agencia.controller;

import com.agencia.agencia.model.Carro;
import com.agencia.agencia.model.DetalleOrdenes;
import com.agencia.agencia.model.Orden;
import com.agencia.agencia.model.OrdenPago;
import com.agencia.agencia.service.PayPalService;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import com.agencia.agencia.service.CarrosService;
import com.agencia.agencia.service.OrdenService;
import com.agencia.agencia.service.OrdenPagoService;



@RestController
@RequestMapping("/api/paypal")
public class PayPalController {

    private final PayPalService payPalService;
    private final CarrosService carrosService;
    private final OrdenService ordenService;
    private final OrdenPagoService ordenPagoService;


    public PayPalController(PayPalService payPalService,OrdenPagoService ordenPagoService,  OrdenService ordenService, CarrosService carrosService) {
        this.payPalService = payPalService;
        this.ordenService = ordenService;
        this.ordenPagoService = ordenPagoService;
        this.carrosService = carrosService; 
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) {
        try {
            System.out.println("📦 create-order body: " + data);

            if (!data.containsKey("total") || data.get("total") == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "El campo 'total' es requerido"
                ));
            }

            Double total = Double.valueOf(data.get("total").toString());

            String jsonResponse = payPalService.createPayPalOrder(total);
            JSONObject json = new JSONObject(jsonResponse);

            if (!json.has("id")) {
                return ResponseEntity.status(500).body(Map.of(
                        "error", "No se recibió un ID válido desde PayPal",
                        "response", jsonResponse
                ));
            }

            String orderId = json.getString("id");

            return ResponseEntity.ok(Map.of("id", orderId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error interno al crear la orden de PayPal",
                    "mensaje", e.getMessage()
            ));
        }
    }

    @PostMapping("/capture-order")
    public ResponseEntity<?> captureOrder(
            @RequestParam String orderID,
            @RequestParam Long ordenId) {
        try {
            System.out.println("🔄 Capturando orden PayPal con ID: " + orderID + ", orden interna: " + ordenId);
            String result = payPalService.captureOrder(orderID, ordenId);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error al capturar la orden de PayPal",
                    "mensaje", e.getMessage()
            ));
        }
    }
    @PostMapping("/capturar")
public ResponseEntity<?> capturarDesdeFactura(@RequestParam String orderID, @RequestParam Long ordenId) {
    System.out.println("➡️ Iniciando captura para orden interna ID = " + ordenId + " y PayPal ID = " + orderID);

    if (orderID == null || orderID.isBlank()) {
        return ResponseEntity.badRequest().body(Map.of("error", "El ID de la orden de PayPal es inválido"));
    }

    Orden orden;
    try {
        orden = ordenService.findById(ordenId);
    } catch (Exception e) {
        return ResponseEntity.status(404).body(Map.of("error", "Orden no encontrada con ID: " + ordenId));
    }

    if (orden.getDetalles() == null || orden.getDetalles().isEmpty()) {
        return ResponseEntity.status(400).body(Map.of("error", "La orden no tiene productos para procesar"));
    }

    orden.setEstadoPago(true);
    ordenService.updateOrder(orden);

    for (DetalleOrdenes detalle : orden.getDetalles()) {
        Carro carro = detalle.getCarro();
        if (carro != null) {
            carro.setDisponibilidad(false);
            carrosService.markAsSold(carro); // Este método lo tenés que tener en tu servicio
        }
    }

    OrdenPago ordenPago = new OrdenPago();
    ordenPago.setOrden(orden);
    ordenPago.setMonto((double) orden.getPrecio());
    ordenPago.setEstado("COMPLETED");
    ordenPago.setFechaPago(LocalDateTime.now());
    ordenPago.setPaypalOrderId(orderID);

    OrdenPago guardada = ordenPagoService.guardar(ordenPago);

    ordenService.createOrder(orden.getUsuario());

    return ResponseEntity.ok(Map.of(
        "mensaje", "Pago realizado con éxito",
        "facturaId", guardada.getId()
    ));
}

    

}
