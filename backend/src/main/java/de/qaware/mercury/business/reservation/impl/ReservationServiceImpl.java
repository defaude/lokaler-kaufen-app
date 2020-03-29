package de.qaware.mercury.business.reservation.impl;

import de.qaware.mercury.business.email.EmailService;
import de.qaware.mercury.business.login.LoginException;
import de.qaware.mercury.business.login.ReservationCancellationToken;
import de.qaware.mercury.business.login.TokenService;
import de.qaware.mercury.business.reservation.Interval;
import de.qaware.mercury.business.reservation.Reservation;
import de.qaware.mercury.business.reservation.ReservationCancellation;
import de.qaware.mercury.business.reservation.ReservationFailedException;
import de.qaware.mercury.business.reservation.ReservationNotFoundException;
import de.qaware.mercury.business.reservation.ReservationService;
import de.qaware.mercury.business.reservation.Slot;
import de.qaware.mercury.business.reservation.SlotService;
import de.qaware.mercury.business.reservation.Slots;
import de.qaware.mercury.business.shop.ContactType;
import de.qaware.mercury.business.shop.Shop;
import de.qaware.mercury.business.shop.ShopNotFoundException;
import de.qaware.mercury.business.shop.ShopService;
import de.qaware.mercury.business.time.Clock;
import de.qaware.mercury.business.uuid.UUIDFactory;
import de.qaware.mercury.storage.reservation.ReservationRepository;
import de.qaware.mercury.util.Lists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class ReservationServiceImpl implements ReservationService {
    private final SlotService slotService;
    private final ReservationRepository reservationRepository;
    private final Clock clock;
    private final UUIDFactory uuidFactory;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final ShopService shopService;

    @Override
    @Transactional(readOnly = true)
    public Slots listSlots(Shop shop, int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("days must not be <= 0, was " + days);
        }

        LocalDate begin = clock.today();
        // if passed day = 1, you'll only get today
        // if passed day = 2, you'll get today and tomorrow
        LocalDate end = begin.plusDays(days - 1L);

        // Find all reservations in the time range
        List<Reservation> reservations = reservationRepository.findReservationsForShop(shop.getId(), begin.atTime(0, 0), end.atTime(23, 59));
        // Now generate slots. The time ranges which also have reservations are marked as unavailable
        return new Slots(
            days,
            begin,
            slotService.generateSlots(begin, end, shop.getSlotConfig(), mapReservations(reservations))
        );
    }

    @Override
    @Transactional
    public void createReservation(Shop shop, Slot.Id slotId, ContactType contactType, String contact, String name, String email) throws ReservationFailedException {
        LocalDateTime start = slotId.toLocalDateTime();
        LocalDateTime end = start.plusMinutes(shop.getSlotConfig().getTimePerSlot());

        // Check if that slot is a valid slot for the shop
        if (!slotService.isValidSlot(start, end, shop.getSlotConfig())) {
            throw new ReservationFailedException(String.format("Invalid slot [%s - %s]", start, end));
        }

        // Check if that slot is already reserved
        if (!reservationRepository.findReservationsForShop(shop.getId(), start, end).isEmpty()) {
            throw new ReservationFailedException(String.format("Slot [%s - %s] is already reserved", start, end));
        }

        // Check if the shop supports the contact type
        if (!shop.getContacts().containsKey(contactType)) {
            throw new ReservationFailedException(String.format("Shop doesn't support contact type %s", contactType));
        }

        Reservation.Id reservationId = Reservation.Id.random(uuidFactory);

        reservationRepository.insert(new Reservation(reservationId, shop.getId(), start, end, contact, email, name, contactType, clock.nowZoned(), clock.nowZoned()));

        log.info("Sending customer reservation confirmation to '{}'", email);
        emailService.sendCustomerReservationConfirmation(shop, email, name, start, end, contactType, contact, reservationId);
        log.info("Sending new shop reservation to '{}'", shop.getEmail());
        emailService.sendShopNewReservation(shop, name, start, end, contactType, contact, reservationId);
    }

    @Override
    @Transactional
    public void cancelReservation(ReservationCancellationToken token) throws ReservationNotFoundException, LoginException, ShopNotFoundException {
        ReservationCancellation cancellation = tokenService.verifyReservationCancellationToken(token);

        Reservation reservation = reservationRepository.findById(cancellation.getReservationId());
        if (reservation == null) {
            throw new ReservationNotFoundException(cancellation.getReservationId());
        }
        Shop shop = shopService.findByIdOrThrow(reservation.getShopId());
        reservationRepository.deleteById(reservation.getId());

        switch (cancellation.getSide()) {
            case SHOP:
                // Shop cancelled, send email to customer
                log.info("Sending reservation cancellation to customer '{}'", reservation.getEmail());
                emailService.sendReservationCancellationToCustomer(shop, reservation);
                // Send cancellation confirmation to shop
                emailService.sendReservationCancellationConfirmation(shop.getEmail(), reservation);
                break;
            case CUSTOMER:
                // Customer cancelled, send email to shop
                log.info("Sending reservation cancellation to shop '{}'", shop.getEmail());
                emailService.sendReservationCancellationToShop(shop, reservation);
                // Send cancellation confirmation to customer
                emailService.sendReservationCancellationConfirmation(reservation.getEmail(), reservation);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + cancellation.getSide());
        }
    }

    private List<Interval> mapReservations(List<Reservation> reservations) {
        return Lists.map(reservations, r -> Interval.of(r.getStart(), r.getEnd()));
    }
}
