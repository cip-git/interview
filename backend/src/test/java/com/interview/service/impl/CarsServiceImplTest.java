package com.interview.service.impl;

import com.interview.domain.entity.Cars;
import com.interview.domain.repository.CarsRepository;
import com.interview.events.CarCreatedEvent;
import com.interview.mapper.CarsMapper;
import com.interview.service.api.CarsService;
import com.interview.validation.groups.OnUpdate;
import com.interview.web.dto.cars.CarsDto;
import com.interview.web.dto.cars.CarsRequest;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarsServiceImplTest {

    public static final String FORD = "Ford";
    public static final String FOCUS = "Focus";
    public static final String VW = "VW";
    public static final String GOLF = "Golf";
    @Mock
    CarsRepository repository;

    @Mock
    CarsMapper mapper;

    @Mock
    ApplicationEventPublisher publisher;

    @Mock
    Validator validator;

    CarsService service;

    @BeforeEach
    void setUp() {
        service = new CarsServiceImpl(repository, mapper, publisher, validator);
    }

    private CarsRequest req() {
        return CarsRequest
                .builder()
                .make(FORD)
                .model(FOCUS)
                .manufactureYear(2018)
                .vin("uu1abc12345678901")
                .odometerKm(12345)
                .build();
    }

    private Cars entity(Long id, int version) {
        return Cars
                .builder()
                .id(id)
                .make(FORD)
                .model(FOCUS)
                .manufactureYear(2018)
                .vin("UU1ABC12345678901")
                .odometerKm(12345)
                .version(version)
                .build();
    }

    private CarsDto dto(Long id, int version) {
        return CarsDto
                .builder()
                .id(id)
                .make(FORD)
                .model(FOCUS)
                .manufactureYear(2018)
                .vin("UU1ABC12345678901")
                .odometerKm(12345)
                .version(version)
                .build();
    }

    @Test
    void create_persists_maps_and_publishes_event() {
        CarsRequest rq = req();
        Cars toSave = entity(null, 0);
        Cars saved = entity(10L, 0);
        CarsDto out = dto(10L, 0);

        when(mapper.map(rq)).thenReturn(toSave);
        when(repository.saveAndFlush(toSave)).thenReturn(saved);
        when(mapper.map(saved)).thenReturn(out);

        ArgumentCaptor<CarCreatedEvent> cap = ArgumentCaptor.forClass(CarCreatedEvent.class);

        CarsDto result = service.create(rq);
        assertThat(result.getId()).isEqualTo(10L);

//        verify(validator).validate(eq(toSave), eq(OnCreate.class));

        verify(publisher).publishEvent(cap.capture());
        assertThat(cap.getValue().id()).isEqualTo(10L);
        assertThat(cap.getValue().vin()).isEqualTo("UU1ABC12345678901");
    }

    @Test
    void create_propagates_unique_violation() {
        CarsRequest rq = req();
        Cars toSave = entity(null, 0);
        when(mapper.map(rq)).thenReturn(toSave);
        when(repository.saveAndFlush(toSave))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> service.create(rq))
                .isInstanceOf(DataIntegrityViolationException.class);
        verifyNoInteractions(publisher);
    }

    @Test
    void getById_ok() {
        when(repository.findById(5L)).thenReturn(Optional.of(entity(5L, 0)));
        when(mapper.map(any(Cars.class))).thenReturn(dto(5L, 0));

        var res = service.getById(5L);
        assertThat(res.getId()).isEqualTo(5L);
    }

    @Test
    void getById_404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void getByVin_normalizes_and_finds() {
        when(repository.findByVin("VIN1234567890123"))
                .thenReturn(Optional.of(entity(7L, 0)));
        when(mapper.map(any(Cars.class))).thenReturn(dto(7L, 0));

        var res = service.getByVin("  vin1234567890123 ");
        assertThat(res.getId()).isEqualTo(7L);
        verify(repository).findByVin("VIN1234567890123");
    }

    @Test
    void list_with_filters_uses_findByMakeAndModel() {
        when(repository.findByMakeAndModel(FORD, FOCUS))
                .thenReturn(List.of(entity(1L, 0), entity(2L, 0)));
        when(mapper.map(any(Cars.class)))
                .thenReturn(dto(1L, 0), dto(2L, 0));

        Page<CarsDto> page = service.list(PageRequest.of(0, 20), FORD, FOCUS);

        assertThat(page.getTotalElements()).isEqualTo(2);
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void list_without_filters_uses_findAll() {
        Page<Cars> pg = new PageImpl<>(List.of(entity(3L, 0)));
        when(repository.findAll(any(Pageable.class))).thenReturn(pg);
        when(mapper.map(any(Cars.class))).thenReturn(dto(3L, 0));

        var res = service.list(PageRequest.of(1, 10), null, null);
        assertThat(res.getContent()).extracting(CarsDto::getId).containsExactly(3L);
    }

    @Test
    void replace_version_mismatch_throws_409() {
        Cars existing = entity(10L, 5);
        when(repository.findById(10L)).thenReturn(Optional.of(existing));

        CarsRequest rq = CarsRequest
                .builder()
                .make("VW")
                .model("Golf")
                .manufactureYear(2019)
                .odometerKm(1000)
                .version(4) // mismatch
                .build();

        assertThatThrownBy(() -> service.replace(10L, rq))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("409");
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void replace_ok_updates_and_saves() {
        Cars existing = entity(10L, 5);
        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(existing)).thenReturn(existing);
        when(mapper.map(existing)).thenReturn(dto(10L, 5));

        CarsRequest rq = CarsRequest
                .builder()
                .make(VW)
                .model(GOLF)
                .manufactureYear(2019)
                .odometerKm(5000)
                .version(5)
                .build();

        var res = service.replace(10L, rq);
        assertThat(res.getId()).isEqualTo(10L);
        assertThat(existing.getMake()).isEqualTo(VW);
        verify(validator).validate(eq(existing), eq(OnUpdate.class));
    }

    @Test
    void patch_calls_mapper_update_and_saves() {
        Cars existing = entity(10L, 1);
        when(repository.findById(10L)).thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(existing)).thenReturn(existing);
        when(mapper.map(existing)).thenReturn(dto(10L, 2));

        CarsRequest rq = CarsRequest.builder()
                .odometerKm(77777)
                .version(1)
                .build();

        var res = service.patch(10L, rq);
        assertThat(res.getId()).isEqualTo(10L);
        verify(mapper).updateEntityFromRequest(rq, existing);
        verify(validator).validate(eq(existing), eq(com.interview.validation.groups.OnPatch.class));
    }

    @Test
    void delete_not_found() {
        when(repository.existsById(77L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(77L))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("404");
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void delete_ok() {
        when(repository.existsById(10L)).thenReturn(true);
        service.delete(10L);
        verify(repository).deleteById(10L);
    }
}
