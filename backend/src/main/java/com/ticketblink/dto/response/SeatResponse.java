package com.ticketblink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SeatResponse {
    private Long id;
    private String seatRow;
    private int seatNumber;
    private String seatType;
    private boolean booked;
}
