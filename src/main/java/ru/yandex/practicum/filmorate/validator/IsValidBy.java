package ru.yandex.practicum.filmorate.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = IsValidByValidator.class)
@Documented
public @interface IsValidBy {
    String message() default "by может принимать только два значения - title и director";


    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
