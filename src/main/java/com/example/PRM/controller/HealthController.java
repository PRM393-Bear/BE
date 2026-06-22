package com.example.PRM.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/api/ping")
    public String ping() {

        jdbcTemplate.execute("SELECT 1");
        return "Pong - Server is alive!";
    }
}
