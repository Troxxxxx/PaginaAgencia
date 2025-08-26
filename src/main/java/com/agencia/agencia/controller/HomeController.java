package com.agencia.agencia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * GET  /home
     * Ruta explícita al login.
     */
    @GetMapping("/home")
    public String home() {
        return "home";  // Thymeleaf: src/main/resources/templates/home.html
    }

    /**
     * GET  /
     * Redirige a /home para que no caiga en Whitelabel.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }
}
