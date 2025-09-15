package com.interview.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Welcome", description = "Welcome API")
public class WelcomeController {

    @GetMapping("/api/welcome")
    public String index() {

        return "Welcome to the interview project!";
    }
}