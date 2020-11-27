package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DemoController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<String> index(){
        return new ResponseEntity<>("Hello world!!!", HttpStatus.OK);
    }

    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public ResponseEntity<String> post(){
        return new ResponseEntity<>("Successfully!!!!", HttpStatus.OK);
    }
}
