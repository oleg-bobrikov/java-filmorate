package ru.yandex.practicum.filmorate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Optional;

public class IsValidByValidator implements ConstraintValidator<IsValidBy, Optional<List<String>>> {
    @Override
    public void initialize(IsValidBy constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Optional<List<String>> value, ConstraintValidatorContext context) {
        if (value.isEmpty()) {
            return true;
        }
        List<String> list = value.get();
        if (list.size() == 1) {
            return list.get(0).equals("director") || list.get(0).equals("title");
        } else if (list.size() == 2) {
            return list.get(0).equals("title") && list.get(1).equals("director")
                    || list.get(0).equals("director") && list.get(1).equals("title");
        } else {
            return false;
        }
    }
}
