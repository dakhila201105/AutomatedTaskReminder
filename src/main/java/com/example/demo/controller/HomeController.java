package com.example.demo.controller;

import com.example.demo.entity.Task;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("message", "Welcome to Spring Boot!");
        model.addAttribute("task", new Task());
        return "add-task";
    }
}
