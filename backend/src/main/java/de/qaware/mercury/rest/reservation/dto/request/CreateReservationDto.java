package de.qaware.mercury.rest.reservation.dto.request;

import de.qaware.mercury.rest.validation.ValidationConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationDto {
    @NotBlank
    private String slotId;
    @NotBlank
    // TODO: Validate enum
    private String contactType;
    @NotBlank
    // TODO validate pattern
    private String contact;
    @NotBlank
    // TODO validate pattern
    private String name;
    @Email(regexp = ValidationConstants.EMAIL_REGEX, flags = Pattern.Flag.CASE_INSENSITIVE)
    @NotNull
    private String email;
}
