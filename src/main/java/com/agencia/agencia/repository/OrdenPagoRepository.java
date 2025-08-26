package com.agencia.agencia.repository;

import com.agencia.agencia.model.Orden;
import com.agencia.agencia.model.OrdenPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrdenPagoRepository extends JpaRepository<OrdenPago, Long> {

    boolean existsByPaypalOrderId(String paypalOrderId);

    Optional<OrdenPago> findByOrden(Orden orden);

    Optional<OrdenPago> findByPaypalOrderId(String paypalOrderId);
}
