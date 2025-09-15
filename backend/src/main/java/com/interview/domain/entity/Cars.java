package com.interview.domain.entity;

import com.interview.validation.Vin;
import com.interview.validation.groups.OnCreate;
import com.interview.validation.groups.OnPatch;
import com.interview.validation.groups.OnUpdate;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "cars",
        uniqueConstraints = @UniqueConstraint(name = "uk_cars_vin", columnNames = "vin"))
public class Cars {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(groups = {OnCreate.class, OnUpdate.class})
    @Size(
            max = 64,
            groups = {OnCreate.class, OnUpdate.class})
    @Column(nullable = false, length = 64)
    private String make;

    @NotBlank(
            groups = {OnCreate.class, OnUpdate.class})
    @Size(
            max = 128,
            groups = {OnCreate.class, OnUpdate.class})
    @Column(nullable = false, length = 128)
    private String model;

    @NotNull(
            groups = {OnCreate.class, OnUpdate.class})
    @Min(
            value = 1886,
            groups = {OnCreate.class, OnUpdate.class})
    @Column(name = "manufacture_year", nullable = false)
    private Integer manufactureYear;

    @Vin(
            groups = {OnCreate.class, OnUpdate.class, OnPatch.class })
    @Column(nullable = false, length = 17, unique = true)
    private String vin;

    @PositiveOrZero
    @Column(name = "odometer_km")
    private Integer odometerKm;

    @NotNull(groups = {OnCreate.class, OnUpdate.class, OnPatch.class})
    @Version
    @Column(name = "row_version")
    private Integer version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void normalize() {
        if (vin != null) vin = vin.trim().toUpperCase();
        if (make != null) make = make.trim();
        if (model != null) model = model.trim();
    }
}
