package com.hotel.validation;

import com.hotel.repository.GuestRepository;
import com.hotel.repository.StaffRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final GuestRepository guestRepository;
    private final StaffRepository staffRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return true; // let @NotBlank / @Email handle null/blank
        }
        return !guestRepository.existsByEmailAndDeletedAtIsNull(email)
            && !staffRepository.existsByEmailAndDeletedAtIsNull(email);
    }
}
