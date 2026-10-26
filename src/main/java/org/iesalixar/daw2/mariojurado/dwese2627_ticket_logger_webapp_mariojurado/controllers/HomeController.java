package org.iesalixar.daw2.mariojurado.dwese2627_ticket_logger_webapp_mariojurado.controllers;

import ch.qos.logback.core.model.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model){
        return "index";
    }
}
