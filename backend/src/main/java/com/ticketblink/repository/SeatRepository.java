package com.ticketblink.repository;

import com.ticketblink.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByVenueIdOrderBySeatRowAscSeatNumberAsc(Long venueId);

    /**
     * Acquire a pessimistic write lock on the requested seats.
     * This prevents two concurrent transactions from booking the same seat.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds")
    List<Seat> findAllByIdForUpdate(@Param("seatIds") List<Long> seatIds);
}
