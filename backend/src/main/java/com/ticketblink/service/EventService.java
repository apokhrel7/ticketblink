package com.ticketblink.service;

import com.ticketblink.dto.response.EventResponse;
import com.ticketblink.dto.response.SeatResponse;
import com.ticketblink.entity.Event;
import com.ticketblink.entity.Seat;
import com.ticketblink.entity.enums.TicketStatus;
import com.ticketblink.exception.ApiException;
import com.ticketblink.repository.EventRepository;
import com.ticketblink.repository.SeatRepository;
import com.ticketblink.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<EventResponse> getUpcomingEvents() {
        return eventRepository.findUpcoming(LocalDateTime.now()).stream()
                .map(this::toEventResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAllOrdered().stream()
                .map(this::toEventResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Event", id));
        return toEventResponse(event);
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsForEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> ApiException.notFound("Event", eventId));

        List<Seat> seats = seatRepository.findByVenueIdOrderBySeatRowAscSeatNumberAsc(event.getVenue().getId());
        Set<Long> bookedSeatIds = new HashSet<>(
                ticketRepository.findBookedSeatIds(eventId, TicketStatus.BOOKED)
        );

        return seats.stream()
                .map(seat -> SeatResponse.builder()
                        .id(seat.getId())
                        .seatRow(seat.getSeatRow())
                        .seatNumber(seat.getSeatNumber())
                        .seatType(seat.getSeatType().name())
                        .booked(bookedSeatIds.contains(seat.getId()))
                        .build())
                .toList();
    }

    private EventResponse toEventResponse(Event event) {
        int totalSeats = event.getVenue().getTotalRows() * event.getVenue().getSeatsPerRow();
        List<Long> bookedIds = ticketRepository.findBookedSeatIds(event.getId(), TicketStatus.BOOKED);

        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .venueName(event.getVenue().getName())
                .venueAddress(event.getVenue().getAddress())
                .venueId(event.getVenue().getId())
                .dateTime(event.getDateTime())
                .imageUrl(event.getImageUrl())
                .standardPrice(event.getStandardPrice())
                .premiumPrice(event.getPremiumPrice())
                .vipPrice(event.getVipPrice())
                .totalSeats(totalSeats)
                .availableSeats(totalSeats - bookedIds.size())
                .build();
    }
}
