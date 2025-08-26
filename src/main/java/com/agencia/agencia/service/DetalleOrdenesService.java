package com.agencia.agencia.service;

import com.agencia.agencia.model.Carro;
import com.agencia.agencia.model.DetalleOrdenes;
import com.agencia.agencia.model.Orden;
import com.agencia.agencia.repository.DetalleOrdenesRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DetalleOrdenesService {

    private final DetalleOrdenesRepository detalleOrdenesRepository;

    public DetalleOrdenesService(DetalleOrdenesRepository detalleOrdenesRepository) {
        this.detalleOrdenesRepository = detalleOrdenesRepository;
    }

    // ✅ Agregar un carro a una orden
    public DetalleOrdenes addCarToOrder(Orden orden, Carro carro) {
        DetalleOrdenes detalle = new DetalleOrdenes();
        detalle.setOrden(orden);
        detalle.setCarro(carro);
        detalle.setCantidad(1);
        detalle.setPrecio_unitario(carro.getPrecio_carro());
        return detalleOrdenesRepository.save(detalle);
    }

    // ✅ Buscar un detalle por su ID (como Long, no int)
    public Optional<DetalleOrdenes> findById(Long id) {
        return detalleOrdenesRepository.findById(id);
    }

    // ✅ Eliminar un detalle
    public void delete(DetalleOrdenes detalle) {
        detalleOrdenesRepository.delete(detalle);
    }
}
