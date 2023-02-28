package ru.yandex.practicum.filmorate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class IsAfterOrEqualValidator implements ConstraintValidator<IsAfterOrEqual, LocalDate> {
    String validDateString;

    @Override
    public void initialize(IsAfterOrEqual constraintAnnotation) {
        validDateString = constraintAnnotation.current();
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        String[] splitDate = validDateString.split("-");
        LocalDate validDate = LocalDate.of(
                Integer.parseInt(splitDate[0]),
                Integer.parseInt(splitDate[1]),
                Integer.parseInt(splitDate[2]));
        return date.isAfter(validDate) || date.isEqual(validDate);
    }
}
