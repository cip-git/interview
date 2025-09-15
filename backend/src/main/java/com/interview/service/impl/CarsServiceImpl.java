package com.interview.service.impl;

import com.interview.domain.entity.Cars;
import com.interview.domain.repository.CarsRepository;
import com.interview.events.CarCreatedEvent;
import com.interview.mapper.CarsMapper;
import com.interview.service.api.CarsService;
import com.interview.validation.groups.OnPatch;
import com.interview.validation.groups.OnUpdate;
import com.interview.web.dto.cars.CarsDto;
import com.interview.web.dto.cars.CarsRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CarsServiceImpl implements CarsService {

    private final CarsRepository repository;
    private final CarsMapper mapper;
    private final ApplicationEventPublisher publisher;
    private final Validator validator;

    @Override
    @Transactional(readOnly = true)
    public Page<CarsDto> list(Pageable pageable, String make, String model) {
        if (make != null && model != null) {
            List<CarsDto> items = repository
                    .findByMakeAndModel(make, model)
                    .stream()
                    .map(mapper::map)
                    .toList();
            return new PageImpl<>(items, pageable, items.size());
        }
        return repository
                .findAll(pageable)
                .map(mapper::map);
    }

    @Override
    @Transactional(readOnly = true)
    public CarsDto getById(Long id) {
        Cars entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cars not found"));
        return mapper.map(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public CarsDto getByVin(String vin) {
        String norm = vin == null ? null : vin.trim().toUpperCase();
        Cars entity = repository
                .findByVin(norm)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cars not found"));
        return mapper.map(entity);
    }

    @Override
    public CarsDto create(CarsRequest req) {
        Cars entity = mapper.map(req);
        Cars entitySaved = repository.saveAndFlush(entity);
        publisher.publishEvent(new CarCreatedEvent(entitySaved.getId(), entitySaved.getVin()));
        return mapper.map(entitySaved);
    }

    @Override
    public CarsDto replace(Long id, CarsRequest req) {
        Cars entity = repository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cars not found"));

        if (!Objects.equals(req.getVersion(), entity.getVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Version mismatch");
        }

        entity.setMake(req.getMake());
        entity.setModel(req.getModel());
        entity.setManufactureYear(req.getManufactureYear());
        entity.setOdometerKm(req.getOdometerKm());
        entity.setVersion(req.getVersion());
        validateOrThrow(entity, OnUpdate.class);
        return mapper.map(repository.saveAndFlush(entity));
    }

    @Override
    public CarsDto patch(Long id, CarsRequest req) {
        Cars entity = repository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cars not found"));

        if (!Objects.equals(req.getVersion(), entity.getVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Version mismatch");
        }

        mapper.updateEntityFromRequest(req, entity);
        validateOrThrow(entity, OnPatch.class);

        return mapper.map(repository.saveAndFlush(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cars not found");
        }
        repository.deleteById(id);
    }

    private void validateOrThrow(Object target, Class<?>... groups) {
        var violations = validator.validate(target, groups);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
