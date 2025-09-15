package com.interview.web.dto.cars;

import com.interview.validation.Vin;
import com.interview.validation.groups.OnCreate;
import com.interview.validation.groups.OnPatch;
import com.interview.validation.groups.OnUpdate;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarsRequest {

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Size(
            max = 64,
            groups = {OnCreate.class, OnUpdate.class})
    @Null(groups = OnPatch.class)
    private String make;

    @NotBlank(
            groups = {OnCreate.class, OnUpdate.class})
    @Size(
            max = 128,
            groups = {OnCreate.class, OnUpdate.class})
    @Null(groups = OnPatch.class)
    private String model;

    @NotNull(
            groups = {OnCreate.class, OnUpdate.class})
    @Min(
            value = 1886,
            groups = {OnCreate.class, OnUpdate.class})
    private Integer manufactureYear;

    @Vin(
            groups = OnCreate.class)
    @Null(
            groups = {OnUpdate.class, OnPatch.class})
    private String vin;

    @PositiveOrZero
    private Integer odometerKm;

    @Null(
            groups = OnCreate.class)
    @NotNull(
            groups = {OnUpdate.class, OnPatch.class})
    private Integer version;
}
