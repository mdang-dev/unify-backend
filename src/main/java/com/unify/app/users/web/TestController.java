package com.unify.app.users.web;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

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

    @GetMapping("/test-3")
    public String getTest2() {
        return "Hi 3";
    }

}
