package com.ticketblink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String eventName;
    private LocalDateTime eventDateTime;
    private BigDecimal totalAmount;
    private String status;
    private List<TicketResponse> tickets;
    private LocalDateTime createdAt;
}
