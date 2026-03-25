package com.ticketblink.seeder;

import com.ticketblink.entity.Event;
import com.ticketblink.entity.Seat;
import com.ticketblink.entity.Venue;
import com.ticketblink.entity.enums.SeatType;
import com.ticketblink.repository.EventRepository;
import com.ticketblink.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;

    @Value("${app.seeder.enabled:true}")
    private boolean enabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled || venueRepository.count() > 0) {
            log.info("Seeder skipped (disabled or data already exists)");
            return;
        }

        log.info("Seeding database with sample data...");

        Venue arena = createVenue("Scotiabank Saddledome", "555 Saddledome Rise SE, Calgary, AB", 10, 15);
        Venue theater = createVenue("Arts Commons", "205 8 Ave SE, Calgary, AB", 8, 12);
        Venue stadium = createVenue("McMahon Stadium", "1817 Crowchild Trail NW, Calgary, AB", 12, 20);

        venueRepository.saveAll(List.of(arena, theater, stadium));

        LocalDateTime now = LocalDateTime.now();

        List<Event> events = List.of(
                Event.builder()
                        .name("Arctic Monkeys - Live in Calgary")
                        .description("The Sheffield legends bring their electrifying live show to Calgary for one unforgettable night.")
                        .venue(arena)
                        .dateTime(now.plusDays(30))
                        .imageUrl("https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?w=800")
                        .standardPrice(new BigDecimal("75.00"))
                        .premiumPrice(new BigDecimal("150.00"))
                        .vipPrice(new BigDecimal("300.00"))
                        .build(),
                Event.builder()
                        .name("Calgary International Film Festival")
                        .description("A curated selection of independent films from around the world, featuring Q&A sessions with directors.")
                        .venue(theater)
                        .dateTime(now.plusDays(14))
                        .imageUrl("https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800")
                        .standardPrice(new BigDecimal("25.00"))
                        .premiumPrice(new BigDecimal("45.00"))
                        .vipPrice(new BigDecimal("80.00"))
                        .build(),
                Event.builder()
                        .name("Stampede Rodeo Finals")
                        .description("The grand finale of the Calgary Stampede rodeo competition. Watch world-class cowboys compete for glory.")
                        .venue(stadium)
                        .dateTime(now.plusDays(45))
                        .imageUrl("https://images.unsplash.com/photo-1560813962-ff3d8fcf59ba?w=800")
                        .standardPrice(new BigDecimal("55.00"))
                        .premiumPrice(new BigDecimal("110.00"))
                        .vipPrice(new BigDecimal("220.00"))
                        .build(),
                Event.builder()
                        .name("Tech Summit YYC 2026")
                        .description("Calgary's premier tech conference featuring keynotes on AI, cloud architecture, and the future of startups.")
                        .venue(theater)
                        .dateTime(now.plusDays(60))
                        .imageUrl("https://images.unsplash.com/photo-1505373877841-8d25f7d46678?w=800")
                        .standardPrice(new BigDecimal("40.00"))
                        .premiumPrice(new BigDecimal("75.00"))
                        .vipPrice(new BigDecimal("150.00"))
                        .build(),
                Event.builder()
                        .name("The Weeknd - After Hours Tour")
                        .description("Abel Tesfaye returns to his Canadian roots for a massive arena show in Calgary.")
                        .venue(arena)
                        .dateTime(now.plusDays(75))
                        .imageUrl("https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=800")
                        .standardPrice(new BigDecimal("95.00"))
                        .premiumPrice(new BigDecimal("200.00"))
                        .vipPrice(new BigDecimal("400.00"))
                        .build()
        );

        eventRepository.saveAll(events);
        log.info("Seeded {} venues and {} events", 3, events.size());
    }

    private Venue createVenue(String name, String address, int rows, int seatsPerRow) {
        Venue venue = Venue.builder()
                .name(name)
                .address(address)
                .totalRows(rows)
                .seatsPerRow(seatsPerRow)
                .build();

        List<Seat> seats = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            String rowLabel = String.valueOf((char) ('A' + r));
            SeatType type = determineSeatType(r, rows);

            for (int s = 1; s <= seatsPerRow; s++) {
                seats.add(Seat.builder()
                        .venue(venue)
                        .seatRow(rowLabel)
                        .seatNumber(s)
                        .seatType(type)
                        .build());
            }
        }

        venue.setSeats(seats);
        return venue;
    }

    /**
     * First 20% of rows = VIP, next 30% = PREMIUM, rest = STANDARD
     */
    private SeatType determineSeatType(int rowIndex, int totalRows) {
        double ratio = (double) rowIndex / totalRows;
        if (ratio < 0.2) return SeatType.VIP;
        if (ratio < 0.5) return SeatType.PREMIUM;
        return SeatType.STANDARD;
    }
}
