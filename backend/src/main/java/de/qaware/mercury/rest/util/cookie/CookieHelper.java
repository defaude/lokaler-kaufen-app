package de.qaware.mercury.rest.util.cookie;

import de.qaware.mercury.business.login.TokenWithExpiry;
import de.qaware.mercury.business.time.Clock;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

@Component
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@EnableConfigurationProperties(CookieConfigurationProperties.class)
public class CookieHelper {
    // Duration to subtract from the lifetime of the token to get lifetime of the cookie. Prevents race conditions
    // where a cookie was valid when it was sent from the client but by the time it reaches the server, the contained
    // token is expired
    private static final Duration COOKIE_LEEWAY = Duration.ofMinutes(1);

    private final Clock clock;
    private final CookieConfigurationProperties cookieConfig;

    public void addTokenCookie(String name, TokenWithExpiry<?> token, HttpServletResponse response) {
        // Java doesn't support setting the SameSite attribute on cookies, so we have
        // to build the header ourself
        StringBuilder cookie = new StringBuilder();
        cookie
            .append(name).append("=").append(token.getToken().getToken()).append("; ")
            .append("Max-Age=").append(token.expiryInSeconds(clock.nowZoned()) - COOKIE_LEEWAY.getSeconds()).append("; ")
            .append("Path=/; ")
            .append("HttpOnly; ")
            .append("SameSite=Lax");

        if (cookieConfig.isSecure()) {
            cookie.append("; Secure");
        }

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
