package com.agencia.agencia.service;

import com.agencia.agencia.model.Orden;
import com.agencia.agencia.model.Usuario;

public interface OrdenService {
    Orden findById(Long id);
    Orden findOrCreateOpenOrder(Usuario usuario);
    Orden createOrder(Usuario usuario);
    void updateOrder(Orden orden);
    void completeOrder(Orden orden);
}
