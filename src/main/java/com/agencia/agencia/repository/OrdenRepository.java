package com.agencia.agencia.repository;

import com.agencia.agencia.model.Orden;
import com.agencia.agencia.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    Optional<Orden> findTopByUsuarioAndEstadoPagoFalseOrderByFechaOrdenDesc(Usuario usuario);
}
