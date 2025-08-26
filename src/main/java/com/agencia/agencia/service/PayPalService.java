package com.agencia.agencia.service;

import com.agencia.agencia.model.*;
import com.agencia.agencia.repository.OrdenPagoRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PayPalService {

    @Value("${paypal.client.id}")
    private String CLIENT_ID;

    @Value("${paypal.client.secret}")
    private String CLIENT_SECRET;

    private final RestTemplate restTemplate = new RestTemplate();
    private final OrdenPagoRepository ordenPagoRepository;
    private final OrdenService ordenService;
    private final CarrosService carrosService;

    public PayPalService(OrdenPagoRepository ordenPagoRepository,
                         OrdenService ordenService,
                         CarrosService carrosService) {
        this.ordenPagoRepository = ordenPagoRepository;
        this.ordenService = ordenService;
        this.carrosService = carrosService;
    }

    public String createPayPalOrder(Double total) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);

        JSONObject amount = new JSONObject();
        amount.put("currency_code", "USD");
        amount.put("value", String.format(Locale.US, "%.2f", total));  // ✅ Usa punto decimal

        JSONObject purchaseUnit = new JSONObject();
        purchaseUnit.put("amount", amount);

        JSONObject order = new JSONObject();
        order.put("intent", "CAPTURE");
        order.put("purchase_units", List.of(purchaseUnit));
        order.put("application_context", Map.of(
                "return_url", "http://localhost:8080/pay/success",
                "cancel_url", "http://localhost:8080/pay/cancel"
        ));

        HttpEntity<String> request = new HttpEntity<>(order.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api-m.sandbox.paypal.com/v2/checkout/orders",
                    request,
                    String.class
            );

            System.out.println("✅ PayPal Order Response: " + response.getBody());

            return response.getBody();

        } catch (Exception e) {
            System.err.println("❌ Error al crear la orden en PayPal:");
            e.printStackTrace();
            throw new RuntimeException("Error al crear la orden de PayPal: " + e.getMessage());
        }
    }

    public String captureOrder(String paypalOrderId, Long ordenId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);
    
        HttpEntity<String> request = new HttpEntity<>("{}", headers);
    
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + paypalOrderId + "/capture",
                request,
                String.class
        );
    
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("❌ Error al capturar la orden: respuesta HTTP " + response.getStatusCode());
        }
    
        JSONObject json = new JSONObject(response.getBody());
    
        // Verificación del estado
        String status = json.optString("status", "UNKNOWN");
        if (!status.equalsIgnoreCase("COMPLETED")) {
            throw new RuntimeException("❌ La orden PayPal no se completó. Estado: " + status);
        }
    
        double amount = json.getJSONArray("purchase_units")
                .getJSONObject(0)
                .getJSONObject("payments")
                .getJSONArray("captures")
                .getJSONObject(0)
                .getJSONObject("amount")
                .getDouble("value");
    
        // Verifica si ya se ha registrado antes
        if (ordenPagoRepository.existsByPaypalOrderId(paypalOrderId)) {
            System.out.println("⚠️ Ya existe una factura con ese PayPal Order ID, omitiendo registro duplicado.");
            return response.getBody();
        }
    
        Orden orden = ordenService.findById(ordenId);
    
        OrdenPago ordenPago = new OrdenPago(
                paypalOrderId,
                orden,
                amount,
                "COMPLETED",
                LocalDateTime.now()
        );
    
        ordenPagoRepository.save(ordenPago);
        ordenService.completeOrder(orden);
    
        for (DetalleOrdenes detalle : orden.getDetalles()) {
            Carro carro = detalle.getCarro();
            if (carro != null) {
                carrosService.markAsSold(carro);
            }
        }
    
        return response.getBody();
    }
    
}
