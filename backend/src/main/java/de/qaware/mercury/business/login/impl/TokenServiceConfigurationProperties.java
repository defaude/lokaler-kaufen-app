package de.qaware.mercury.business.login.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "mercury.tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
class TokenServiceConfigurationProperties {
    @NotBlank
    private String shopJwtSecret;
    @NotBlank
    private String adminJwtSecret;
    @NotBlank
    private String shopCreationJwtSecret;
    @NotBlank
    private String passwordResetJwtSecret;
    @NotBlank
    private String reservationCancellationJwtSecret;
}
