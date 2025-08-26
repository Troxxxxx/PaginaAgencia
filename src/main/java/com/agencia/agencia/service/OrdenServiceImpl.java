package com.agencia.agencia.service;

import com.agencia.agencia.model.Orden;
import com.agencia.agencia.model.Usuario;
import com.agencia.agencia.repository.OrdenRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository ordenRepository;

    public OrdenServiceImpl(OrdenRepository ordenRepository) {
        this.ordenRepository = ordenRepository;
    }

    @Override
    public Orden findById(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden con ID " + id + " no encontrada"));
    }

    @Override
    @Transactional
    public Orden findOrCreateOpenOrder(Usuario usuario) {
        Optional<Orden> openOrder = ordenRepository.findTopByUsuarioAndEstadoPagoFalseOrderByFechaOrdenDesc(usuario);
        Orden orden = openOrder.orElseGet(() -> createOrder(usuario));
        Hibernate.initialize(orden.getDetalles());
        if (orden.getDetalles() != null) {
            orden.getDetalles().forEach(detalle -> {
                Hibernate.initialize(detalle.getCarro());
                if (detalle.getCarro() != null) {
                    Hibernate.initialize(detalle.getCarro().getModelo());
                    Hibernate.initialize(detalle.getCarro().getMarca());
                }
            });
        }
        return orden;
    }

    @Override
    @Transactional
    public Orden createOrder(Usuario usuario) {
        Orden orden = new Orden();
        orden.setUsuario(usuario);
        orden.setFecha_orden(LocalDate.now());
        orden.setEstadoPago(false);
        orden.setPrecio(0);
        return ordenRepository.save(orden);
    }

    @Override
    @Transactional
    public void updateOrder(Orden orden) {
        ordenRepository.save(orden);
    }

    @Override
    @Transactional
    public void completeOrder(Orden orden) {
        orden.setEstadoPago(false);
        ordenRepository.save(orden);
    }
}
