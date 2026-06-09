package org.example.service;

import org.example.dto.request.CreateBookingRequest;
import org.example.dto.response.BookingResponse;
import org.example.entity.Booking;
import org.example.entity.Court;
import org.example.entity.User;
import org.example.repository.BookingRepository;
import org.example.repository.CourtRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private CourtRepository courtRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public BookingResponse createBooking(CreateBookingRequest request, Long userId) {
        // Validate user exists
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found");
        }
        
        // Validate court exists
        Optional<Court> courtOpt = courtRepository.findById(request.getCourtId());
        if (!courtOpt.isPresent()) {
            throw new RuntimeException("Court not found");
        }
        
        Court court = courtOpt.get();
        
        // Check for conflicting bookings
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
            request.getCourtId(),
            request.getBookingDate(),
            request.getStartTime(),
            request.getEndTime()
        );
        
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Court is not available for the selected time slot");
        }
        
        // Create booking
        Booking booking = Booking.builder()
                .user(userOpt.get())
                .court(court)
                .bookingDate(request.getBookingDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalPrice(calculatePrice(request))
                .status("CONFIRMED")
                .notes(request.getNotes())
                .build();
        
        Booking savedBooking = bookingRepository.save(booking);
        return convertToDTO(savedBooking);
    }
    
    public BookingResponse getBooking(Long bookingId, Long userId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        
        if (!bookingOpt.isPresent()) {
            throw new RuntimeException("Booking not found");
        }
        
        Booking booking = bookingOpt.get();
        
        // Verify ownership
        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to this booking");
        }
        
        return convertToDTO(booking);
    }
    
    public List<BookingResponse> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<BookingResponse> getUpcomingBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdAndBookingDateGreaterThanEqual(
            userId,
            LocalDate.now()
        );
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public void cancelBooking(Long bookingId, Long userId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        
        if (!bookingOpt.isPresent()) {
            throw new RuntimeException("Booking not found");
        }
        
        Booking booking = bookingOpt.get();
        
        // Verify ownership
        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to this booking");
        }
        
        // Check if booking can be cancelled
        if ("CANCELLED".equals(booking.getStatus()) || "COMPLETED".equals(booking.getStatus())) {
            throw new RuntimeException("Cannot cancel this booking");
        }
        
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }
    
    public List<BookingResponse> getCourtBookings(Long courtId, LocalDate date) {
        List<Booking> bookings = bookingRepository.findByCourtIdAndBookingDate(courtId, date);
        return bookings.stream()
                .filter(b -> !b.getStatus().equals("CANCELLED"))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private double calculatePrice(CreateBookingRequest request) {
        // Calculate duration in hours
        long duration = java.time.temporal.ChronoUnit.HOURS.between(
            request.getStartTime(),
            request.getEndTime()
        );
        
        // Get court price per hour (assuming 50 per hour as default)
        double pricePerHour = 50.0;
        
        return duration * pricePerHour;
    }
    
    private BookingResponse convertToDTO(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getUsername())
                .courtId(booking.getCourt().getId())
                .courtName(booking.getCourt().getCourtName())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
