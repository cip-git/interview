package com.interview.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {})
@Target({ FIELD, PARAMETER, METHOD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@ReportAsSingleViolation
@NotBlank
@Size(min = 17, max = 17)
@Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$")
public @interface Vin {

    String message() default "VIN invalid (17 caractere, fara I/O/Q)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
