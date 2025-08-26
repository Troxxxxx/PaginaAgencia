package com.agencia.agencia.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.agencia.agencia.model.Marca;
import com.agencia.agencia.service.MarcaService;

@Controller
@RequestMapping("/marcas")
public class MarcaController {

    private final MarcaService marcaService;

    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @GetMapping("/marca")
    public String listaMarca(Model model) {
        model.addAttribute("listaMarca", marcaService.listarMarca());
        return "listaMarca";
    }

    @GetMapping("/nuevo")
    public String crearNuevaMarca(Model model) {
        model.addAttribute("opcionesCarro", new Marca());
        return "opcionesCarro";
    }

    @PostMapping("/guardar")
    public String guardarMarca(@ModelAttribute Marca marca) {
        marcaService.add(marca);
        return "redirect:/marcas/marca";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable int id, Model model) {
        Marca marca = marcaService.consultar(id);
        model.addAttribute("marcas", marca);
        return "editarMarca";
    }

    @PostMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarMarca(@PathVariable int id) {
        try {
            marcaService.eliminar(id);
            return ResponseEntity.ok().body(Map.of("success", true)); 
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "¡No se puede eliminar esta marca! Está asociada a un modelo o carro."
            ));
        }
    }
}