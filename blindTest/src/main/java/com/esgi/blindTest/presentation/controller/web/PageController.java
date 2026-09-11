package com.esgi.blindTest.presentation.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Ajout au diagramme : les quatre pages Thymeleaf. Elles ne portent aucune regle,
 * elles appellent l'API REST.
 */
@Controller
public class PageController {

    @GetMapping("/")
    public String accueil() {
        return "redirect:/blindtests";
    }

    @GetMapping("/inscription")
    public String inscription() {
        return "inscription";
    }

    @GetMapping("/connexion")
    public String connexion() {
        return "connexion";
    }

    @GetMapping("/blindtests")
    public String blindTests() {
        return "blindtests";
    }

    @GetMapping("/blindtests/{id}")
    public String salle(@PathVariable Long id, Model model) {
        model.addAttribute("id", id);
        return "salle";
    }
}
