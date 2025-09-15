package com.interview.web.dto.cars;

import com.interview.common.Identifiable;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CarsDto implements Identifiable<Long> {
    private Long id;
    private String make;
    private String model;
    private Integer manufactureYear;
    private String vin;
    private Integer odometerKm;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
