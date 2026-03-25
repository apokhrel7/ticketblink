package com.ticketblink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events", indexes = {
    @Index(name = "idx_event_date", columnList = "dateTime"),
    @Index(name = "idx_event_venue", columnList = "venue_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    private String imageUrl;

    @Column(nullable = false)
    private BigDecimal standardPrice;

    @Column(nullable = false)
    private BigDecimal premiumPrice;

    @Column(nullable = false)
    private BigDecimal vipPrice;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
