package com.irctc.booking_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookingController {

    @GetMapping("/")
    public String home() {
        return "IRCTC Booking Service Running 🚆";
    }
}