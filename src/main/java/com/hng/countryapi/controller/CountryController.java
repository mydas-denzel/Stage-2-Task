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
    public ResponseEntity<?> getCountries(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String sort
    ) {
        // Handle "null" (as a string) or empty query params
        if ("null".equalsIgnoreCase(region)) region = null;
        if ("null".equalsIgnoreCase(currency)) currency = null;
        if ("null".equalsIgnoreCase(sort)) sort = null;

        try {
            List<Country> countries = service.getCountries(region, currency, sort);

            if (countries.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No countries found matching your criteria"));
            }

            return ResponseEntity.ok(countries);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Validation failed",
                            "details", Map.of("sort", "Invalid sort format. Use gdp_asc or gdp_desc")
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Something went wrong", "details", e.getMessage()));
        }
    }


    @GetMapping("/{name}")
    public ResponseEntity<?> getCountryByName(@PathVariable String name) {
        return service.getCountryByName(name)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Country not found")));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteCountry(@PathVariable String name) {
        boolean deleted = service.deleteCountry(name);

        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Country not found"));
        }
    }


    @GetMapping("/image")
    public ResponseEntity<?> getSummaryImage() {
        File file = new File("/tmp/summary.png");
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
