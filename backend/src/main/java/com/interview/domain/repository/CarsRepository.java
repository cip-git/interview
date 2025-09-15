package com.interview.domain.repository;


import com.interview.domain.entity.Cars;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarsRepository extends JpaRepository<Cars, Long> {

    Optional<Cars> findByVin(String vin);

    List<Cars> findByMakeAndModel(String make, String model);
}
