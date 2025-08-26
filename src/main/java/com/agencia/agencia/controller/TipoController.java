package com.agencia.agencia.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.agencia.agencia.model.Tipo;
import com.agencia.agencia.service.TipoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping("/tipos")
public class TipoController {

    private final TipoService tipoService;

    public TipoController(TipoService tipoService) {
        this.tipoService = tipoService;
    }

    @GetMapping("/tipo")
    public String listaTipo(Model model) {
        model.addAttribute("listaTipo", tipoService.listarTipo());
        return "listaTipo";
    }

    @GetMapping("/nuevo")
    public String crearNuevoTipo(Model model) {
        model.addAttribute("opcionesCarro", new Tipo());
        return "opcionesCarro";
    }

    @PostMapping("/guardar")
    public String guardarTipo(@ModelAttribute Tipo tipo) {
        tipoService.add(tipo);
        return "redirect:/tipos/tipo";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable int id, Model model) {
        Tipo tipo = tipoService.consultar(id);
        model.addAttribute("tipos", tipo);
        return "editarTipo";
    }

    @PostMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarTipo(@PathVariable int id) {
        try {
            tipoService.eliminar(id); 
            return ResponseEntity.ok().body(Map.of("success", true)); 
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "¡No se puede eliminar este tipo! Está asociado a un carro o modelo." 
            ));
        }
    }

}