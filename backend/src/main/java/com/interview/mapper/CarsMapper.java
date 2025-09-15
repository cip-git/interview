package com.interview.mapper;

import com.interview.domain.entity.Cars;
import com.interview.web.dto.cars.CarsDto;
import com.interview.web.dto.cars.CarsRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CarsMapper {

    CarsDto map(Cars entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Cars map(CarsRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vin", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(CarsRequest req, @MappingTarget Cars entity);

}
