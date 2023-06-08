package com.example.sarabrandserver.test;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "test")
@Profile(value = {"dev"})
public class TestController {

    @GetMapping(path = "/client")
    public String client() {
        return "client";
    }

    @GetMapping(path = "/worker")
    public String worker() {
        return "worker";
    }

}
