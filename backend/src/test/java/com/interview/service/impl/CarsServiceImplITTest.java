package com.interview.service.impl;

import com.interview.events.listener.CarEventListener;
import com.interview.service.api.CarsService;
import com.interview.web.dto.cars.CarsDto;
import com.interview.web.dto.cars.CarsRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("dev")
class CarsServiceImplITTest {

    @Autowired
    private CarsService service;

    @SpyBean
    private CarEventListener listener;

    private static CarsRequest req(
            String make,
            String model,
            int year,
            String vin,
            Integer odo) {
        return CarsRequest.builder()
                .make(make)
                .model(model)
                .manufactureYear(year)
                .vin(vin)
                .odometerKm(odo)
                .build();
    }

    private static String vin(int n) {
        String base = "UU1ABC1234567890";
        char last = (char) ('A' + (n % 26));
        if (last == 'I' || last == 'O' || last == 'Q') last = 'Z';
        return base + last;
    }

    @Test
    void create_inserts_and_publishes_event_after_commit() {
        CarsRequest rq = req("Dacia", "Duster", 2021, vin(1), 10);

        CarsDto created = service.create(rq);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getVin()).isEqualTo(rq.getVin().toUpperCase());

        verify(listener, timeout(500)).onCarCreated(any());
    }

    @Test
    void create_duplicateVin_throws_DataIntegrityViolationException() {
        String sameVin = vin(2);
        service.create(req("VW", "Golf", 2019, sameVin, 100));

        assertThatThrownBy(
                () -> service.create(req("VW", "Golf", 2019, sameVin.toLowerCase(), 200)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void getById_then_replace_updates_and_increments_version() {
        CarsDto created = service.create(req("Toyota", "Corolla", 2020, vin(3), 1000));

        CarsDto before = service.getById(created.getId());

        CarsRequest replaceRq = CarsRequest
                .builder()
                .make("Toyota")
                .model("Corolla H")
                .manufactureYear(2021)
                .odometerKm(1500)
                .version(before.getVersion())
                .build();

        CarsDto after = service.replace(created.getId(), replaceRq);

        assertThat(after.getModel()).isEqualTo("Corolla H");
        assertThat(after.getManufactureYear()).isEqualTo(2021);
        assertThat(after.getVersion()).isEqualTo(before.getVersion() + 1);
    }

    @Test
    void replace_with_wrong_version_throws_409() {
        CarsDto created = service.create(req("Mazda", "3", 2022, vin(4), 50));

        CarsRequest badRq = CarsRequest.builder()
                .make("Mazda")
                .model("3 Hatch")
                .manufactureYear(2023)
                .odometerKm(60)
                .version(-1)
                .build();

        assertThatThrownBy(() -> service.replace(created.getId(), badRq))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    void patch_updates_partial_fields_and_respects_version() {
        CarsDto created = service.create(req("Skoda", "Octavia", 2018, vin(5), 5000));
        CarsDto current = service.getById(created.getId());

        CarsRequest patchRq = CarsRequest.builder()
                .odometerKm(77777)
                .version(current.getVersion())
                .build();

        CarsDto patched = service.patch(created.getId(), patchRq);

        assertThat(patched.getOdometerKm()).isEqualTo(77777);
        assertThat(patched.getVersion()).isEqualTo(current.getVersion() + 1);
        assertThat(patched.getVin()).isEqualTo(created.getVin());
    }

    @Test
    void getByVin_normalizes_input() {
        String v = vin(6);
        service.create(req("Renault", "Megane", 2017, v, 10));

        CarsDto byVin = service.getByVin("   " + v.toLowerCase() + "   ");
        assertThat(byVin.getVin()).isEqualTo(v.toUpperCase());
    }
}
