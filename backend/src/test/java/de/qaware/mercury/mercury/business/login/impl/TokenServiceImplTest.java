package de.qaware.mercury.mercury.business.login.impl;

import de.qaware.mercury.mercury.business.admin.Admin;
import de.qaware.mercury.mercury.business.login.AdminToken;
import de.qaware.mercury.mercury.business.login.LoginException;
import de.qaware.mercury.mercury.business.login.PasswordResetToken;
import de.qaware.mercury.mercury.business.login.ShopCreationToken;
import de.qaware.mercury.mercury.business.login.ShopLogin;
import de.qaware.mercury.mercury.business.login.ShopToken;
import de.qaware.mercury.mercury.business.shop.Shop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TokenServiceImplTest {
    private TokenServiceImpl sut;

    @BeforeEach
    void setUp() {
        TokenServiceConfigurationProperties config = new TokenServiceConfigurationProperties("shop-secret", "admin-secret", "shop-creation-secret", "password-reset-secret");
        sut = new TokenServiceImpl(config);
    }

    @Test
    void admin_token() throws LoginException {
        Admin.Id id = Admin.Id.of(UUID.randomUUID());
        AdminToken token = sut.createAdminToken(id);
        System.out.println(token);

        Admin.Id idFromToken = sut.verifyAdminToken(token);
        assertThat(idFromToken).isEqualTo(id);
    }

    @Test
    void shop_token() throws LoginException {
        ShopLogin.Id shopLoginId = ShopLogin.Id.of(UUID.randomUUID());
        Shop.Id shopId = Shop.Id.of(UUID.randomUUID());
        ShopToken token = sut.createShopToken(shopLoginId, shopId);
        System.out.println(token);

        ShopLogin.Id idFromToken = sut.verifyShopToken(token);
        assertThat(idFromToken).isEqualTo(shopLoginId);
    }

    @Test
    void shop_creation_token() throws LoginException {
        ShopCreationToken token = sut.createShopCreationToken("foo@bar.org");
        System.out.println(token);

        String email = sut.verifyShopCreationToken(token);
        assertThat("foo@bar.org").isEqualTo(email);
    }

    @Test
    void password_reset_token() throws LoginException {
        PasswordResetToken token = sut.createPasswordResetToken("foo@bar.org");
        System.out.println(token);

        String email = sut.verifyPasswordResetToken(token);
        assertThat("foo@bar.org").isEqualTo(email);
    }
}