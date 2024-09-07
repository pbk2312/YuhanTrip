package hello.yuhanTrip.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class ErrorController {


    @GetMapping("/error")
    public String test(){
        return "error";
    }
}
