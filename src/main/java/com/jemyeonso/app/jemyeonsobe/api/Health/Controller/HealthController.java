package com.jemyeonso.app.jemyeonsobe.api.Health.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    //헬스체크
    @GetMapping("")
    public String health() {
        return "Health OK";
    }
}