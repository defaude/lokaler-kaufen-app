package de.qaware.mercury.business.reservation.impl

import de.qaware.mercury.mercury.business.email.EmailService
import de.qaware.mercury.mercury.business.reservation.ReservationService
import de.qaware.mercury.mercury.business.reservation.Slot
import de.qaware.mercury.mercury.business.reservation.SlotService
import de.qaware.mercury.mercury.business.shop.ContactType
import de.qaware.mercury.mercury.business.shop.Shop
import de.qaware.mercury.mercury.business.shop.SlotConfig
import de.qaware.mercury.mercury.business.time.Clock
import de.qaware.mercury.mercury.business.uuid.UUIDFactory
import de.qaware.mercury.mercury.storage.reservation.ReservationRepository
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.time.LocalDateTime

@ContextConfiguration(classes = ReservationServiceImpl)
class ReservationServiceImplSpec extends Specification {

    @Autowired
    ReservationService reservationService

    @SpringBean
    SlotService slotService = Mock()
    @SpringBean
    ReservationRepository reservationRepository = Mock()
    @SpringBean
    Clock clock = Mock()
    @SpringBean
    UUIDFactory uuidFactory = Mock()
    @SpringBean
    EmailService emailService = Mock()

    def "List available slots for shop"() {
        given:
        def today = LocalDateTime.now().toLocalDate()
        def id = Shop.Id.of(UUID.randomUUID())
        def shop = new Shop.ShopBuilder().id(id).slotConfig(SlotConfig.builder().build()).build()

        when:
        def slots = reservationService.listSlots(shop)

        then:
        1 * clock.today() >> today
        1 * reservationRepository.findReservationsForShop(id, today.atTime(0, 0), today.atTime(23, 59)) >> []
        1 * slotService.generateSlots(today, today, shop.getSlotConfig(), []) >> []
        slots.size() == 0
    }

    def "Create Reservation for Shop"() {
        given:
        def shopId = Shop.Id.of(UUID.randomUUID())
        def shop = new Shop.ShopBuilder().id(shopId).slotConfig(SlotConfig.builder().build()).build()
        def slotId = Slot.Id.of(LocalDateTime.now())

        when:
        reservationService.createReservation(shop, slotId, ContactType.WHATSAPP, 'Contact', 'Spock', 'test@lokaler.kaufen')

        then:
        1 * uuidFactory.create() >> UUID.randomUUID()
        1 * reservationRepository.insert(_)
        1 * emailService.sendCustomerReservationConfirmation(shop, 'test@lokaler.kaufen', 'Spock', _, _, ContactType.WHATSAPP, 'Contact')
        1 * emailService.sendShopNewReservation(shop, 'Spock', _, _, ContactType.WHATSAPP, 'Contact')
    }
}
