package de.qaware.mercury.business.reservation;

import de.qaware.mercury.business.login.LoginException;
import de.qaware.mercury.business.login.ReservationCancellationToken;
import de.qaware.mercury.business.shop.Breaks;
import de.qaware.mercury.business.shop.ContactType;
import de.qaware.mercury.business.shop.Shop;
import de.qaware.mercury.business.shop.ShopNotFoundException;
import de.qaware.mercury.business.shop.SlotConfig;

import java.util.Set;

public interface ReservationService {
    Slots listSlots(Shop shop, int days);

    /**
     * Anonymize all reservations that ended the day before.
     *
     * @return the number of entries anonymized.
     */
    int anonymizeExpired();

    void createReservation(Shop shop, Slot.Id slotId, ContactType contactType, String contact, String name, String email) throws ReservationFailedException;

    void cancelReservation(ReservationCancellationToken token) throws ReservationNotFoundException, LoginException, ShopNotFoundException;

    Slots previewSlots(SlotConfig slotConfig);

    Breaks resolveBreaks(Set<String> slotIds, SlotConfig slotConfig);
}
