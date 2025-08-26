package com.agencia.agencia.service;

import com.agencia.agencia.model.Orden;
import com.agencia.agencia.model.OrdenPago;
import com.agencia.agencia.repository.OrdenPagoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrdenPagoService {

    private final OrdenPagoRepository ordenPagoRepository;

    public OrdenPagoService(OrdenPagoRepository ordenPagoRepository) {
        this.ordenPagoRepository = ordenPagoRepository;
    }

    public OrdenPago guardar(OrdenPago ordenPago) {
        return ordenPagoRepository.save(ordenPago);
    }

    public Optional<OrdenPago> buscarPorId(Long id) {
        return ordenPagoRepository.findById(id);
    }

    public Optional<OrdenPago> buscarPorOrden(Orden orden) {
        return ordenPagoRepository.findByOrden(orden);
    }

    public Optional<OrdenPago> buscarPorPaypalOrderId(String paypalOrderId) {
        return ordenPagoRepository.findByPaypalOrderId(paypalOrderId);
    }

    public boolean existePorPaypalOrderId(String paypalOrderId) {
        return ordenPagoRepository.existsByPaypalOrderId(paypalOrderId);
    }
}
