package com.unify.app.users.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestController {

    @GetMapping("/test")
    public String getTest() {
        return "Hi";
    }

    @GetMapping("/test-2")
    public String getTest1() {
        return "Hi 2";
    }
}
