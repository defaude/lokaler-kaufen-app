package de.qaware.mercury.business.reservation;

import de.qaware.mercury.business.shop.SlotConfig;

import java.time.LocalDate;
import java.util.List;

public interface SlotService {

    /**
     * Generates a list of available time slots from a given slot config which are between the start and the end date.
     * If the start or end time of an existing reservation is within a time slot, it is omitted from the list.
     *
     * @param start                The start date
     * @param end                  The end date, must be after start
     * @param slotConfig           The slot configuration
     * @param existingReservations Any existing reservations
     * @return A list of available time slots between the start and end date.
     */
    List<Slot> generateSlots(LocalDate start, LocalDate end, SlotConfig slotConfig, List<Interval> existingReservations);
}
