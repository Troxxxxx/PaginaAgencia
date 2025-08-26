package com.agencia.agencia.controller;

import com.agencia.agencia.dto.*;
import com.agencia.agencia.model.*;
import com.agencia.agencia.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/cart")
public class CartController {

    private final CarrosService carrosService;
    private final OrdenService ordenService;
    private final DetalleOrdenesService detalleOrdenesService;
    private final UsuarioService usuarioService;
    private final OrdenPagoService ordenPagoService;


    public CartController(CarrosService carrosService,  OrdenService ordenService,
                          DetalleOrdenesService detalleOrdenesService, UsuarioService usuarioService,  OrdenPagoService ordenPagoService) {
        this.carrosService = carrosService;
        this.ordenService = ordenService;
        this.detalleOrdenesService = detalleOrdenesService;
        this.usuarioService = usuarioService;
        this.ordenPagoService = ordenPagoService;
    }

    @GetMapping("/view")
public ResponseEntity<OrdenDTO> viewCart() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal().equals("anonymousUser")) {
        return ResponseEntity.status(401).build(); // Unauthorized
    }

    String email = auth.getName();
    Usuario usuario = usuarioService.findByCorreo(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Orden orden = ordenService.findOrCreateOpenOrder(usuario);

    // Filtrar los detalles cuyos carros no estén disponibles
    orden.getDetalles().removeIf(detalle -> !detalle.getCarro().isDisponibilidad());

    OrdenDTO ordenDTO = mapToOrdenDTO(orden);
    return ResponseEntity.ok(ordenDTO);
}


    @PostMapping("/add")
    public ResponseEntity<Void> addToCart(@RequestParam("carroId") int carroId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        String email = auth.getName();
        Usuario usuario = usuarioService.findByCorreo(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Orden orden = ordenService.findOrCreateOpenOrder(usuario);
        Carro carro = carrosService.consultar(carroId);
        if (carro == null || !carro.isDisponibilidad()) {
            return ResponseEntity.status(400).build(); // Bad Request
        }

        DetalleOrdenes detalle = detalleOrdenesService.addCarToOrder(orden, carro);
        orden.setPrecio(orden.getPrecio() + carro.getPrecio_carro());
        ordenService.updateOrder(orden);

        return ResponseEntity.ok().build();
    }


    @PostMapping("/checkout")
public ResponseEntity<?> checkout() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getPrincipal().equals("anonymousUser")) {
        return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
    }

    String email = auth.getName();
    Usuario usuario = usuarioService.findByCorreo(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    Orden orden = ordenService.findOrCreateOpenOrder(usuario);
    if (orden.getDetalles().isEmpty() || orden.getPrecio() == 0) {
        return ResponseEntity.status(400).body(Map.of("error", "Carrito vacío o sin monto válido"));
    }

    for (DetalleOrdenes detalle : orden.getDetalles()) {
        carrosService.markAsSold(detalle.getCarro());
    }

    orden.setEstadoPago(true);
    ordenService.updateOrder(orden);

    OrdenPago ordenPago = new OrdenPago();
    ordenPago.setOrden(orden);
    ordenPago.setMonto((double) orden.getPrecio());
    ordenPago.setEstado("Pagada");
    ordenPago.setFechaPago(LocalDateTime.now());
    ordenPago.setPaypalOrderId("PAGO_SANDBOX_" + UUID.randomUUID());
    ordenPagoService.guardar(ordenPago);

    ordenService.createOrder(usuario);

    return ResponseEntity.ok(Map.of(
        "mensaje", "Pago realizado con éxito",
        "ordenId", orden.getId_orden()
    ));
}

@PostMapping("/api/paypal/capture-order")
public ResponseEntity<?> capturarOrdenPaypal(@RequestParam String orderID, @RequestParam Long ordenId) {
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

    // Validar disponibilidad antes de marcar como vendida
    for (DetalleOrdenes detalle : orden.getDetalles()) {
        Carro carro = detalle.getCarro();
        if (carro == null || !carro.isDisponibilidad()) {
            return ResponseEntity.status(400).body(Map.of("error", "Uno de los carros ya fue vendido o es inválido"));
        }
    }

    // Marcar los carros como vendidos
    for (DetalleOrdenes detalle : orden.getDetalles()) {
        carrosService.markAsSold(detalle.getCarro());
    }

    // Actualizar estado de la orden
    orden.setEstadoPago(true);
    ordenService.updateOrder(orden);

    // Crear y guardar la factura (OrdenPago)
    OrdenPago ordenPago = new OrdenPago();
    ordenPago.setOrden(orden);
    ordenPago.setMonto((double) orden.getPrecio());
    ordenPago.setEstado("COMPLETED");
    ordenPago.setFechaPago(LocalDateTime.now());
    ordenPago.setPaypalOrderId(orderID);

    OrdenPago guardada = ordenPagoService.guardar(ordenPago);

    // Crear nueva orden vacía
    ordenService.createOrder(orden.getUsuario());

    // Confirmación con ID correcto
    return ResponseEntity.ok(Map.of(
        "mensaje", "Pago realizado con éxito",
        "facturaId", guardada.getId()  // 👈 Asegúrate de que esto es lo que el JS espera
    ));
}




    

    // Métodos de mapeo
    private OrdenDTO mapToOrdenDTO(Orden orden) {
        OrdenDTO ordenDTO = new OrdenDTO();
        ordenDTO.setIdOrden(orden.getId_orden());
        ordenDTO.setFechaOrden(orden.getFecha_orden());
        ordenDTO.setPrecio((double) orden.getPrecio()); // Convertimos int a double
        ordenDTO.setEstadoPago(orden.isEstadoPago());
        if (orden.getDetalles() != null) {
            ordenDTO.setDetalles(orden.getDetalles().stream()
                    .map(this::mapToDetalleOrdenDTO)
                    .collect(Collectors.toList()));
        }
        return ordenDTO;
    }

    private DetalleOrdenDTO mapToDetalleOrdenDTO(DetalleOrdenes detalle) {
        DetalleOrdenDTO detalleDTO = new DetalleOrdenDTO();
        detalleDTO.setIdDetalle(detalle.getId_detalle());
        detalleDTO.setCantidad(detalle.getCantidad());
        detalleDTO.setPrecioUnitario(detalle.getPrecio_unitario());
        if (detalle.getCarro() != null) {
            detalleDTO.setCarro(mapToCarroDTO(detalle.getCarro()));
        }
        return detalleDTO;
    }

    private CarroDTO mapToCarroDTO(Carro carro) {
        CarroDTO carroDTO = new CarroDTO();
        carroDTO.setIdCarro(carro.getId_carro());
        carroDTO.setPrecioCarro(carro.getPrecio_carro());
        carroDTO.setAno(carro.getAno());
        if (carro.getModelo() != null) {
            carroDTO.setModelo(mapToModeloDTO(carro.getModelo()));
        }
        if (carro.getMarca() != null) {
            carroDTO.setMarca(mapToMarcaDTO(carro.getMarca()));
        }
        if (carro.getImagenCarros() != null) {
            carroDTO.setRutaImagen(carro.getImagenCarros().getRuta_imagen());
        } else {
            carroDTO.setRutaImagen("assets/img/default-car.jpg");
        }
        return carroDTO;
    }

    private ModeloDTO mapToModeloDTO(Modelo modelo) {
        ModeloDTO modeloDTO = new ModeloDTO();
        modeloDTO.setIdModelo(modelo.getId_modelo());
        modeloDTO.setNombreModelo(modelo.getNombre_modelo());
        return modeloDTO;
    }

    private MarcaDTO mapToMarcaDTO(Marca marca) {
        MarcaDTO marcaDTO = new MarcaDTO();
        marcaDTO.setIdMarca(marca.getId_marca());
        marcaDTO.setNombreMarca(marca.getNombre_marca());
        return marcaDTO;
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestParam("detalleId") Long detalleId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
    
        Optional<DetalleOrdenes> detalleOpt = detalleOrdenesService.findById(detalleId);
    
        if (detalleOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Detalle no encontrado"));
        }
    
        DetalleOrdenes detalle = detalleOpt.get();
        Orden orden = detalle.getOrden();
        if (orden == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Detalle sin orden asociada"));
        }
    
        orden.setPrecio((int) (orden.getPrecio() - detalle.getPrecio_unitario()));
        ordenService.updateOrder(orden);
        detalleOrdenesService.delete(detalle);
    
        return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado del carrito"));
    }
    

    
    
}