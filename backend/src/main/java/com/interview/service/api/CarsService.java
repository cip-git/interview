package com.interview.service.api;

import com.interview.web.dto.cars.CarsDto;
import com.interview.web.dto.cars.CarsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarsService {
    Page<CarsDto> list(Pageable pageable, String make, String model);

    CarsDto getById(Long id);

    CarsDto getByVin(String vin);

    CarsDto create(CarsRequest req);

    CarsDto replace(Long id, CarsRequest req);

    CarsDto patch(Long id, CarsRequest req);

    void delete(Long id);
}
