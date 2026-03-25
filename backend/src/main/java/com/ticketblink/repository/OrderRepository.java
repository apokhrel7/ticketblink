package com.ticketblink.repository;

import com.ticketblink.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o JOIN FETCH o.tickets t JOIN FETCH t.seat WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithTickets(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o JOIN FETCH o.tickets t JOIN FETCH t.seat WHERE o.id = :orderId AND o.user.id = :userId")
    Optional<Order> findByIdAndUserIdWithTickets(@Param("orderId") Long orderId, @Param("userId") Long userId);
}
