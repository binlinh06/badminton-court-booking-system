package org.example.repository;

import org.example.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdAndBookingDateGreaterThanEqual(Long userId, LocalDate bookingDate);
    List<Booking> findByCourtIdAndBookingDate(Long courtId, LocalDate bookingDate);
    List<Booking> findByStatus(String status);
    
    @Query("SELECT b FROM Booking b WHERE b.court.id = :courtId AND b.bookingDate = :date " +
           "AND b.startTime < :endTime AND b.endTime > :startTime AND b.status != 'CANCELLED'")
    List<Booking> findConflictingBookings(
        @Param("courtId") Long courtId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
}
