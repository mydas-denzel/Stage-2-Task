package com.hng.countryapi.controller;

import com.hng.countryapi.repo.CountryRepo;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/status")
public class StatusController {

    private final CountryRepo countryRepository;

    public StatusController(CountryRepo countryRepository) {
        this.countryRepository = countryRepository;
    }

    @GetMapping
    public ResponseEntity<?> getStatus() {
        long total = countryRepository.count();

        Map<String, Object> response = new HashMap<>();
        response.put("total_countries", total);
        response.put("last_refreshed_at", LocalDateTime.now()); // you can replace with your global refresh timestamp

        return ResponseEntity.ok(response);
    }
}
