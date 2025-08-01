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

    @GetMapping("/test-3")
    public String getTest2() {
        return "Hi 3";
    }

    @GetMapping("/test-4")
    public String getTest3() {
        return "Hi 3";
    }
    @GetMapping("/test-5")
    public String getTest4() {
        return "Hi 3";
    }
}
