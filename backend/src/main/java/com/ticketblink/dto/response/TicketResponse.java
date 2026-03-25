package com.ticketblink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class TicketResponse {
    private Long id;
    private String seatRow;
    private int seatNumber;
    private String seatType;
    private BigDecimal price;
    private String status;
}
