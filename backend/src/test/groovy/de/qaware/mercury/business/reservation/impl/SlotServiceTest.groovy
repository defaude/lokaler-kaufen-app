package de.qaware.mercury.business.reservation.impl

import de.qaware.mercury.business.reservation.Interval
import de.qaware.mercury.business.reservation.Slot
import de.qaware.mercury.business.reservation.SlotService
import de.qaware.mercury.business.reservation.Slots
import de.qaware.mercury.business.shop.Breaks
import de.qaware.mercury.business.shop.DayConfig
import de.qaware.mercury.business.shop.SlotConfig
import de.qaware.mercury.business.time.Clock
import de.qaware.mercury.test.builder.SlotConfigBuilder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

import static java.time.LocalDateTime.now

class SlotServiceTest extends Specification {
    Clock clock = Mock()

    @Subject
    SlotService slotService = new SlotServiceImpl(clock)

    @Unroll
    def "Check Empty Slot Generation: #usecase"() {
        when:
        List<Slot> slots = slotService.generateSlots(start.toLocalDate(), end.toLocalDate(), config, [])

        then:
        slots.isEmpty()

        where:
        usecase         | start | end   | config
        'No Slot time'  | now() | now() | new SlotConfigBuilder().setTimePerSlot(0).build()
        'No Day config' | now() | now() | new SlotConfigBuilder().setTimePerSlot(1).build()
    }

    @Unroll
    def "Check Slot Generation: #usecase"() {
        when:
        List<Slot> slots = slotService.generateSlots(start, end, config, reservations)

        then:
        // Set the clock to 00:00 of the starting day to always get the same number of slots
        _ * clock.now() >> LocalDateTime.of(start, LocalTime.of(0, 0, 0))
        slots.size() == count

        where:
        usecase                   | start       | end         | config                            | reservations || count
        'Monday'                  | monday()    | monday()    | mondayConfig(10, 23)              | []           || 13
        'Tuesday'                 | tuesday()   | tuesday()   | tuesdayConfig(8, 18)              | []           || 10
        'Wednesday'               | wednesday() | wednesday() | wednesdayConfig(8, 18)            | []           || 10
        'Thursday'                | thursday()  | thursday()  | thursdayConfig(8, 18)             | []           || 10
        'Friday'                  | friday()    | friday()    | fridayConfig(8, 18)               | []           || 10
        'Saturday'                | saturday()  | saturday()  | saturdayConfig(8, 18)             | []           || 10
        'Sunday'                  | sunday()    | sunday()    | sundayConfig(8, 18)               | []           || 10
        'Monday til Wednesday'    | monday()    | wednesday() | mondayTilWednesday(8, 18)         | []           || 30
        'Monday (no pause)'       | monday()    | monday()    | mondayConfigNoPauses(8, 18)       | []           || 20

        // This is a special one. We don't want to get slots which have already started. Since we set the clock to
        // 00:00, the first available slot should not be part of the returned slot list.
        'Monday (after midnight)' | monday()    | monday()    | mondayConfig(0, 4)                | []           || 3

        // Another special one. Having an end time close to midnight (23:30) and a pause. This combination is a nice
        // edge case, to see if we are handling the end-of-day correctly.
        'Monday (til midnight)'   | monday()    | monday()    | mondayLate()                      | []           || 43

        // This is a special one. We don't want to get slots which have already started. Since we set the clock to
        // 00:00, the first available slot should not be part of the returned slot list.
        'Monday (after midnight)' | monday()    | monday()    | mondayPauseEndExactlyOnMidnight() | []           || 3
    }

    // The current calculation would return an empty list of slots if called with an start date after the end date.
    // To avoid silent errors due to mixed up dates, the method should throw an exception.
    def "Start dates must be before end dates."() {
        when:
        slotService.generateSlots(sunday(), saturday(), saturdayConfig(8, 18), new ArrayList<Interval>())
        then:
        IllegalArgumentException _ = thrown()
    }

    def "test breaks"() {
        given: "a fixed date"
        clock.now() >> LocalDateTime.parse("2020-04-13T13:09:39.940482")
        SlotConfig slotConfig = mondayConfig(10, 12);

        when: "we preview the slots"
        Slots preview = slotService.previewSlots(slotConfig)

        then: "we get two slots, starting next monday"
        // Next monday (see clock above)
        preview.begin == LocalDate.of(2020, 4, 20)
        preview.days == 7
        // 10:00 - 10:30
        preview.slots[0].start == LocalDateTime.of(2020, 4, 20, 10, 0)
        preview.slots[0].end == LocalDateTime.of(2020, 4, 20, 10, 30)
        // 11:00 - 11:30
        preview.slots[1].start == LocalDateTime.of(2020, 4, 20, 11, 0)
        preview.slots[1].end == LocalDateTime.of(2020, 4, 20, 11, 30)

        when: "we resolve these slots to breaks"
        Breaks breaks = slotService.resolveBreaks(Set.of(preview.slots[0].id, preview.slots[1].id), slotConfig)

        then: "we get breaks containing exactly the both slots"
        breaks.getMonday() == Set.of(
            Breaks.Break.of(LocalTime.of(10, 0), LocalTime.of(10, 30)), // Slot 1
            Breaks.Break.of(LocalTime.of(11, 0), LocalTime.of(11, 30)) // Slot 2
        )

        when: "we convert the breaks back to slots"
        List<Slot> slots = slotService.convertBreaksToSlots(breaks)

        then: "we get exactly these slots back"
        slots.size() == 2
        slots[0].start == LocalDateTime.of(2020, 4, 20, 10, 0)
        slots[0].end == LocalDateTime.of(2020, 4, 20, 10, 30)
        slots[1].start == LocalDateTime.of(2020, 4, 20, 11, 0)
        slots[1].end == LocalDateTime.of(2020, 4, 20, 11, 30)
    }

    def "test first slot delay"() {
        given: "a fixed date"
        clock.now() >> LocalDateTime.parse("2020-04-13T09:30:00")

        when: "we generate slots"
        // Monday from 10 to 12, 30 minutes length, 30 minutes pause, 60 min delay
        SlotConfig slotConfig = new SlotConfigBuilder()
            .setMonday(new DayConfig(LocalTime.of(10, 0), LocalTime.of(12, 0)))
            .setTimeBetweenSlots(30).setTimePerSlot(30).setDelayForFirstSlot(60)
            .build()
        List<Slot> slots = slotService.generateSlots(LocalDate.of(2020, 4, 13), LocalDate.of(2020, 4, 13), slotConfig, [])

        then: "the first slot delay is honored"
        // We only have one slot, as it's 09:30, and we wanted a delay of 60 minutes. Therefore the first possible slot
        // would be at 10:30 - but according to the given slot config, this is the slot at 11:00
        slots.size() == 1
        slots[0].start == LocalDateTime.of(2020, 4, 13, 11, 0)
        slots[0].end == LocalDateTime.of(2020, 4, 13, 11, 30)
    }

    static LocalDate monday() {
        return now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .toLocalDate()
    }

    static SlotConfig mondayConfig(int startHour, int endHour) {
        return defaultSlot()
            .setMonday(new DayConfig(LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)))
            .build()
    }

    static SlotConfig mondayLate() {
        return new SlotConfigBuilder()
            .setTimePerSlot(15)
            .setTimeBetweenSlots(5)
            .setMonday(new DayConfig(LocalTime.of(9, 00), LocalTime.of(23, 30)))
            .build()
    }

    static SlotConfig mondayPauseEndExactlyOnMidnight() {
        return new SlotConfigBuilder()
            .setTimePerSlot(15)
            .setTimeBetweenSlots(5)
            .setMonday(new DayConfig(LocalTime.of(23, 00), LocalTime.of(23, 55)))
            .build()
    }

    static SlotConfig mondayConfigNoPauses(int startHour, int endHour) {
        return new SlotConfigBuilder()
            .setTimePerSlot(30)
            .setTimeBetweenSlots(0)
            .setMonday(new DayConfig(LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)))
            .build()
    }

    static LocalDate tuesday() {
        return monday()
            .with(TemporalAdjusters.next(DayOfWeek.TUESDAY))
    }

    static SlotConfig tuesdayConfig(int startHour, int endHour) {
        return defaultSlot()
            .setTuesday(new DayConfig(LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)))
            .build()
    }

    static LocalDate wednesday() {
        return monday()
            .with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY))
    }

    static SlotConfig wednesdayConfig(int startHour, int endHour) {
        return defaultSlot()
            .setWednesday(new DayConfig(LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)))
            .build()
    }

    static LocalDate thursday() {
        return monday()
            .with(TemporalAdjusters.next(DayOfWeek.THURSDAY))
    }

    static SlotConfig thursdayConfig(int startHour, int endHour) {
        return defaultSlot()
            .setThursday(new DayConfig(LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)))
            .build()
    }

    static LocalDate friday() {
        return monday()
            .with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
    }

    static SlotConfig fridayConfig(int startHour, int endHour) {
        return defaultSlot()
            .setFriday(new DayConfig(LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)))
            .build()
    }

    static LocalDate saturday() {
        return monday()
            .with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
    }

    static SlotConfig saturdayConfig(int startHour, int endHour) {
        return defaultSlot()
            .setSaturday(new DayConfig(LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)))
            .build()
    }

    static LocalDate sunday() {
        return monday()
            .with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
    }

    static SlotConfig sundayConfig(int startHour, int endHour) {
        return defaultSlot()
            .setSunday(new DayConfig(LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)))
            .build()
    }

    static SlotConfig mondayTilWednesday(int startHour, int endHour) {
        return defaultSlot()
            .setMonday(new DayConfig(LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)))
            .setTuesday(new DayConfig(LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)))
            .setWednesday(new DayConfig(LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)))
            .build()
    }

    private static SlotConfigBuilder defaultSlot() {
        return new SlotConfigBuilder()
            .setTimePerSlot(30)
            .setTimeBetweenSlots(30)
    }
}
