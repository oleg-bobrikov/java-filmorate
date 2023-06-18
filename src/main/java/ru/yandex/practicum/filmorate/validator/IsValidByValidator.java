package ru.yandex.practicum.filmorate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class IsValidByValidator implements ConstraintValidator<IsValidBy, List<String>> {
    @Override
    public void initialize(IsValidBy constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value.size() == 1) {
            return value.get(0).equals("director") || value.get(0).equals("title");
        } else if (value.size() == 2) {
            return value.get(0).equals("title") && value.get(1).equals("director")
                    || value.get(0).equals("director") && value.get(1).equals("title");
        } else {
            return false;
        }
    }
}
