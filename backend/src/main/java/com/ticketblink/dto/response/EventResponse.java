package com.ticketblink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class EventResponse {
    private Long id;
    private String name;
    private String description;
    private String venueName;
    private String venueAddress;
    private Long venueId;
    private LocalDateTime dateTime;
    private String imageUrl;
    private BigDecimal standardPrice;
    private BigDecimal premiumPrice;
    private BigDecimal vipPrice;
    private int totalSeats;
    private int availableSeats;
}
