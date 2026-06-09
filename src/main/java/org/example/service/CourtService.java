package org.example.service;

import org.example.dto.request.CreateCourtRequest;
import org.example.dto.request.UpdateCourtRequest;
import org.example.dto.response.CourtResponse;
import org.example.entity.Court;
import org.example.repository.CourtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourtService {
    
    @Autowired
    private CourtRepository courtRepository;
    
    public CourtResponse createCourt(CreateCourtRequest request) {
        // Check if court name already exists
        if (courtRepository.findByCourtName(request.getCourtName()).isPresent()) {
            throw new RuntimeException("Court with this name already exists");
        }
        
        Court court = Court.builder()
                .courtName(request.getCourtName())
                .courtNumber(request.getCourtNumber())
                .location(request.getLocation())
                .pricePerHour(request.getPricePerHour())
                .capacity(request.getCapacity())
                .description(request.getDescription())
                .isActive(true)
                .build();
        
        Court savedCourt = courtRepository.save(court);
        return convertToDTO(savedCourt);
    }
    
    public CourtResponse getCourtById(Long courtId) {
        Optional<Court> courtOpt = courtRepository.findById(courtId);
        
        if (!courtOpt.isPresent()) {
            throw new RuntimeException("Court not found");
        }
        
        return convertToDTO(courtOpt.get());
    }
    
    public CourtResponse getCourtByName(String courtName) {
        Optional<Court> courtOpt = courtRepository.findByCourtName(courtName);
        
        if (!courtOpt.isPresent()) {
            throw new RuntimeException("Court not found");
        }
        
        return convertToDTO(courtOpt.get());
    }
    
    public List<CourtResponse> getAllActiveCourts() {
        List<Court> courts = courtRepository.findByIsActiveTrue();
        return courts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<CourtResponse> getAvailableCourtsByDate(LocalDate date) {
        List<Court> courts = courtRepository.findAvailableCourtsByDate(date);
        return courts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public CourtResponse updateCourt(Long courtId, UpdateCourtRequest request) {
        Optional<Court> courtOpt = courtRepository.findById(courtId);
        
        if (!courtOpt.isPresent()) {
            throw new RuntimeException("Court not found");
        }
        
        Court court = courtOpt.get();
        
        // Update fields if provided
        if (request.getCourtName() != null && !request.getCourtName().isEmpty()) {
            // Check if new name is unique (if different from current)
            if (!request.getCourtName().equals(court.getCourtName())) {
                if (courtRepository.findByCourtName(request.getCourtName()).isPresent()) {
                    throw new RuntimeException("Court with this name already exists");
                }
            }
            court.setCourtName(request.getCourtName());
        }
        
        if (request.getCourtNumber() != null) {
            court.setCourtNumber(request.getCourtNumber());
        }
        
        if (request.getLocation() != null && !request.getLocation().isEmpty()) {
            court.setLocation(request.getLocation());
        }
        
        if (request.getPricePerHour() != null && request.getPricePerHour() > 0) {
            court.setPricePerHour(request.getPricePerHour());
        }
        
        if (request.getCapacity() != null && request.getCapacity() > 0) {
            court.setCapacity(request.getCapacity());
        }
        
        if (request.getDescription() != null) {
            court.setDescription(request.getDescription());
        }
        
        if (request.getIsActive() != null) {
            court.setIsActive(request.getIsActive());
        }
        
        Court updatedCourt = courtRepository.save(court);
        return convertToDTO(updatedCourt);
    }
    
    public void deactivateCourt(Long courtId) {
        Optional<Court> courtOpt = courtRepository.findById(courtId);
        
        if (!courtOpt.isPresent()) {
            throw new RuntimeException("Court not found");
        }
        
        Court court = courtOpt.get();
        court.setIsActive(false);
        courtRepository.save(court);
    }
    
    public void activateCourt(Long courtId) {
        Optional<Court> courtOpt = courtRepository.findById(courtId);
        
        if (!courtOpt.isPresent()) {
            throw new RuntimeException("Court not found");
        }
        
        Court court = courtOpt.get();
        court.setIsActive(true);
        courtRepository.save(court);
    }
    
    private CourtResponse convertToDTO(Court court) {
        return CourtResponse.builder()
                .id(court.getId())
                .courtName(court.getCourtName())
                .courtNumber(court.getCourtNumber())
                .location(court.getLocation())
                .pricePerHour(court.getPricePerHour())
                .capacity(court.getCapacity())
                .description(court.getDescription())
                .isActive(court.getIsActive())
                .createdAt(court.getCreatedAt())
                .updatedAt(court.getUpdatedAt())
                .build();
    }
}
