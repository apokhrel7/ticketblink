package com.ticketblink.entity;

import com.ticketblink.entity.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats", indexes = {
    @Index(name = "idx_seat_venue", columnList = "venue_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_seat_venue_row_number", columnNames = {"venue_id", "seatRow", "seatNumber"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private String seatRow;  // "A", "B", "C", ...

    @Column(nullable = false)
    private int seatNumber;  // 1, 2, 3, ...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType seatType;
}
