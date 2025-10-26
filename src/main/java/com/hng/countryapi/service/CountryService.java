package com.hng.countryapi.service;


import com.hng.countryapi.model.Country;
import com.hng.countryapi.repo.CountryRepo;
import com.hng.countryapi.util.ImageGenerator;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CountryService {

    private final CountryRepo repository;
    private final RestTemplate restTemplate = new RestTemplate();

    public CountryService(CountryRepo repository) {
        this.repository = repository;
    }

    public void refreshData() throws IOException {
        String countriesUrl = "https://restcountries.com/v2/all?fields=name,capital,region,population,flag,currencies";
        String exchangeUrl = "https://open.er-api.com/v6/latest/USD";

        try {
            // Fetch JSON data
            Map<String, Object>[] rawCountries = restTemplate.getForObject(countriesUrl, Map[].class);
            Map<String, Object> ratesResponse = restTemplate.getForObject(exchangeUrl, Map.class);

            if (rawCountries == null)
                throw new IOException("No country data received");
            if (ratesResponse == null || !ratesResponse.containsKey("rates"))
                throw new IOException("Exchange rates unavailable");

            Map<String, Double> exchangeRates = (Map<String, Double>) ratesResponse.get("rates");
            LocalDateTime now = LocalDateTime.now();

            for (Map<String, Object> c : rawCountries) {
                try {
                    String name = (String) c.get("name");
                    String capital = (String) c.get("capital");
                    String region = (String) c.get("region");
                    Long population = c.get("population") != null ? ((Number) c.get("population")).longValue() : 0L;
                    String flag = (String) c.get("flag");

                    // Handle currencies safely
                    String currencyCode = null;
                    Double exchangeRate = null;
                    Double estimatedGdp = 0.0;

                    Object currField = c.get("currencies");
                    if (currField instanceof List<?> list && !list.isEmpty()) {
                        Object first = list.get(0);
                        if (first instanceof Map<?, ?> map) {
                            currencyCode = (String) map.get("code");
                        }
                    } else if (currField instanceof Map<?, ?> map) {
                        currencyCode = (String) map.get("code");
                    }

                    if (currencyCode != null && exchangeRates.containsKey(currencyCode)) {
                        exchangeRate = exchangeRates.get(currencyCode);
                        double multiplier = ThreadLocalRandom.current().nextDouble(1000, 2000);
                        estimatedGdp = (population * multiplier) / exchangeRate;
                    }

                    Country existing = repository.findByNameIgnoreCase(name).orElse(null);
                    Country country = existing != null ? existing : new Country();

                    country.setName(name);
                    country.setCapital(capital);
                    country.setRegion(region);
                    country.setPopulation(population);
                    country.setCurrencyCode(currencyCode);
                    country.setExchangeRate(exchangeRate);
                    country.setEstimatedGdp(estimatedGdp);
                    country.setFlagUrl(flag);
                    country.setLastRefreshedAt(now);

                    repository.save(country);

                } catch (Exception inner) {
                    System.err.println("Skipping malformed entry: " + c.get("name") + " - " + inner.getMessage());
                }
            }

            ImageGenerator.generateSummaryImage(repository);

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Failed to fetch external API data: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
        }

    }

    @PostConstruct
    public void disableSslVerification() {
        try {
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                    new javax.net.ssl.X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    }
            };
            javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            System.out.println("⚠️ SSL verification disabled for debugging");
        } catch (Exception ignored) {}
    }



    public List<Country> getCountries(String region, String currency, String sort) {
        List<Country> countries;

        if (region != null)
            countries = repository.findByRegionIgnoreCase(region);
        else if (currency != null)
            countries = repository.findByCurrencyCodeIgnoreCase(currency);
        else
            countries = repository.findAll();

        if ("gdp_desc".equalsIgnoreCase(sort))
            countries.sort(Comparator.comparing(Country::getEstimatedGdp, Comparator.nullsLast(Double::compareTo)).reversed());

        return countries;
    }

    public Optional<Country> getCountryByName(String name) {
        return repository.findByNameIgnoreCase(name);
    }

    public void deleteCountry(String name) {
        repository.findByNameIgnoreCase(name).ifPresent(repository::delete);
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("total_countries", repository.count());
        status.put("last_refreshed_at",
                repository.findAll().stream()
                        .map(Country::getLastRefreshedAt)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null));
        return status;
    }
}
