package com.photon.content.location.repository;

import com.photon.content.location.entity.City;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CityRepository extends ReactiveCrudRepository<City, Long> {
    Flux<City> findByDistrictId(Long districtId);
    Flux<City> findByNameContainingIgnoreCase(String name);
    Flux<City> findByNameContainingIgnoreCaseAndDistrictId(String name, Long districtId);
    Flux<City> findByPinCodeAndDistrictId(String pinCode, Long districtId);
    Flux<City> findByPinCode(String pinCode);
}