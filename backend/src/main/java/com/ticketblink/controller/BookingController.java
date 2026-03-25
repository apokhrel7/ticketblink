package com.ticketblink.controller;

import com.ticketblink.dto.request.BookingRequest;
import com.ticketblink.dto.response.OrderResponse;
import com.ticketblink.entity.User;
import com.ticketblink.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<OrderResponse> bookSeats(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BookingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bookSeats(user, request));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.getUserOrders(user));
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(bookingService.cancelOrder(user, orderId));
    }
}
