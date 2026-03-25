package com.ticketblink.repository;

import com.ticketblink.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.dateTime > :now ORDER BY e.dateTime ASC")
    List<Event> findUpcoming(LocalDateTime now);

    @Query("SELECT e FROM Event e ORDER BY e.dateTime ASC")
    List<Event> findAllOrdered();
}
