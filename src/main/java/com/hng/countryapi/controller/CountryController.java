package com.hng.countryapi.controller;

import com.hng.countryapi.model.Country;
import com.hng.countryapi.service.CountryService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/countries")
public class CountryController {

    private final CountryService service;

    public CountryController(CountryService service) {
        this.service = service;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshCountries() {
        try {
            service.refreshData();
            return ResponseEntity.ok(Map.of("message", "Countries refreshed successfully"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "External data source unavailable", "details", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Country>> getCountries(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(service.getCountries(region, currency, sort));
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> getCountryByName(@PathVariable String name) {
        return service.getCountryByName(name)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Country not found")));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteCountry(@PathVariable String name) {
        service.deleteCountry(name);
        return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
    }

    @GetMapping("/image")
    public ResponseEntity<?> getSummaryImage() {
        File file = new File("cache/summary.png");
        if (!file.exists())
            return ResponseEntity.status(404).body(Map.of("error", "Summary image not found"));

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(file));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(service.getStatus());
    }
}
