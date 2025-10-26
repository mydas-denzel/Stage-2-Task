package com.hng.countryapi.repo;


import com.hng.countryapi.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CountryRepo extends JpaRepository<Country, Long> {

    Optional<Country> findByNameIgnoreCase(String name);

    List<Country> findByRegionIgnoreCase(String region);

    List<Country> findByCurrencyCodeIgnoreCase(String currencyCode);

    @Query("SELECT c FROM Country c ORDER BY c.estimatedGdp DESC")
    List<Country> findTopByGdp(org.springframework.data.domain.Pageable pageable);
}
