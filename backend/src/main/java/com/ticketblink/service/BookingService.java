package com.ticketblink.service;

import com.ticketblink.dto.request.BookingRequest;
import com.ticketblink.dto.response.OrderResponse;
import com.ticketblink.dto.response.TicketResponse;
import com.ticketblink.entity.*;
import com.ticketblink.entity.enums.OrderStatus;
import com.ticketblink.entity.enums.TicketStatus;
import com.ticketblink.exception.ApiException;
import com.ticketblink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final OrderRepository orderRepository;

    /**
     * Books seats for an event within a single ACID transaction.
     * Uses pessimistic locking (SELECT ... FOR UPDATE) on seats to prevent
     * two concurrent users from booking the same seat.
     */
    @Transactional
    public OrderResponse bookSeats(User user, BookingRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> ApiException.notFound("Event", request.getEventId()));

        // 1. Acquire pessimistic write lock on the requested seats
        List<Seat> seats = seatRepository.findAllByIdForUpdate(request.getSeatIds());

        if (seats.size() != request.getSeatIds().size()) {
            throw ApiException.badRequest("One or more selected seats do not exist");
        }

        // 2. Verify none of the seats are already booked for this event
        for (Seat seat : seats) {
            if (ticketRepository.existsByEventIdAndSeatIdAndStatus(event.getId(), seat.getId(), TicketStatus.BOOKED)) {
                throw ApiException.conflict(
                        "Seat " + seat.getSeatRow() + seat.getSeatNumber() + " is already booked"
                );
            }
        }

        // 3. Calculate total and create order
        BigDecimal total = seats.stream()
                .map(seat -> getPriceForSeat(event, seat))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(user)
                .event(event)
                .totalAmount(total)
                .build();

        // 4. Create tickets and add to order
        for (Seat seat : seats) {
            Ticket ticket = Ticket.builder()
                    .order(order)
                    .event(event)
                    .seat(seat)
                    .price(getPriceForSeat(event, seat))
                    .build();
            order.getTickets().add(ticket);
        }

        Order saved = orderRepository.save(order);
        return toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(User user) {
        return orderRepository.findByUserIdWithTickets(user.getId()).stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional
    public OrderResponse cancelOrder(User user, Long orderId) {
        Order order = orderRepository.findByIdAndUserIdWithTickets(orderId, user.getId())
                .orElseThrow(() -> ApiException.notFound("Order", orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw ApiException.badRequest("Order is already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.getTickets().forEach(t -> t.setStatus(TicketStatus.CANCELLED));

        return toOrderResponse(orderRepository.save(order));
    }

    private BigDecimal getPriceForSeat(Event event, Seat seat) {
        return switch (seat.getSeatType()) {
            case VIP -> event.getVipPrice();
            case PREMIUM -> event.getPremiumPrice();
            case STANDARD -> event.getStandardPrice();
        };
    }

    private OrderResponse toOrderResponse(Order order) {
        List<TicketResponse> tickets = order.getTickets().stream()
                .map(t -> TicketResponse.builder()
                        .id(t.getId())
                        .seatRow(t.getSeat().getSeatRow())
                        .seatNumber(t.getSeat().getSeatNumber())
                        .seatType(t.getSeat().getSeatType().name())
                        .price(t.getPrice())
                        .status(t.getStatus().name())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .eventName(order.getEvent().getName())
                .eventDateTime(order.getEvent().getDateTime())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .tickets(tickets)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
