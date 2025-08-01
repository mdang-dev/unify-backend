package com.unify.app.users.web;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
class TestController {

    @GetMapping("/test")
    public String getTest() {
        return "Hi";
    }

}
