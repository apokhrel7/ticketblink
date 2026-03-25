package com.ticketblink.repository;

import com.ticketblink.entity.Ticket;
import com.ticketblink.entity.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t.seat.id FROM Ticket t WHERE t.event.id = :eventId AND t.status = :status")
    List<Long> findBookedSeatIds(@Param("eventId") Long eventId, @Param("status") TicketStatus status);

    boolean existsByEventIdAndSeatIdAndStatus(Long eventId, Long seatId, TicketStatus status);
}
