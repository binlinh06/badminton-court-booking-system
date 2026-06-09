package org.example.repository;

import org.example.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    Optional<Court> findByCourtName(String courtName);
    List<Court> findByIsActiveTrue();
    
    @Query("SELECT c FROM Court c WHERE c.isActive = true AND c.id NOT IN " +
           "(SELECT b.court.id FROM Booking b WHERE b.bookingDate = :date AND b.status != 'CANCELLED')")
    List<Court> findAvailableCourtsByDate(@Param("date") LocalDate date);
}
