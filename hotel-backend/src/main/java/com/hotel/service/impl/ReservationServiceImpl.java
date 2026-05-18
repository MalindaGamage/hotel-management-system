package com.hotel.service.impl;

import com.hotel.dto.request.CreateReservationRequest;
import com.hotel.dto.response.PagedResponse;
import com.hotel.dto.response.ReservationResponse;
import com.hotel.entity.*;
import com.hotel.entity.Reservation.*;
import com.hotel.exception.BookingConflictException;
import com.hotel.exception.BusinessException;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.mapper.ReservationMapper;
import com.hotel.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final GuestRepository guestRepository;
    private final HotelRepository hotelRepository;
    private final ReservationMapper reservationMapper;

    public PagedResponse<ReservationResponse> getReservations(
            Long hotelId, ReservationStatus status, String guestSearch,
            LocalDate checkIn, LocalDate checkOut, Pageable pageable) {
        return PagedResponse.of(reservationRepository
            .findWithFilters(hotelId, status, guestSearch, checkIn, checkOut, pageable)
            .map(reservationMapper::toResponse));
    }

    public ReservationResponse getById(Long id) {
        return reservationMapper.toResponse(findReservation(id));
    }

    public ReservationResponse getByConfirmationNumber(String cn) {
        return reservationMapper.toResponse(
            reservationRepository.findByConfirmationNumberAndDeletedAtIsNull(cn)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Reservation with confirmation " + cn + " not found")));
    }

    public List<ReservationResponse> getExpectedArrivals(Long hotelId, LocalDate date) {
        return reservationRepository.findExpectedArrivals(hotelId, date).stream()
            .map(reservationMapper::toResponse).toList();
    }

    public List<ReservationResponse> getExpectedDepartures(Long hotelId, LocalDate date) {
        return reservationRepository.findExpectedDepartures(hotelId, date).stream()
            .map(reservationMapper::toResponse).toList();
    }

    @Transactional
    public ReservationResponse create(CreateReservationRequest req) {
        Hotel hotel = hotelRepository.findById(req.hotelId())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel", req.hotelId()));
        Guest guest = guestRepository.findByIdAndDeletedAtIsNull(req.guestId())
            .orElseThrow(() -> new ResourceNotFoundException("Guest", req.guestId()));

        // Validate availability for each requested room (@FutureDateRange already checked dates)
        List<Room> rooms = req.rooms().stream().map(sel -> {
            Room room = roomRepository.findByIdAndDeletedAtIsNull(sel.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", sel.roomId()));
            List<Room> available = roomRepository.findAvailableRooms(
                req.hotelId(), req.checkInDate(), req.checkOutDate(), null);
            if (available.stream().noneMatch(r -> r.getId().equals(sel.roomId()))) {
                throw new BookingConflictException(sel.roomId(),
                    req.checkInDate().toString(), req.checkOutDate().toString());
            }
            return room;
        }).toList();

        Reservation reservation = Reservation.builder()
            .hotel(hotel).guest(guest)
            .confirmationNumber(generateConfirmation())
            .status(ReservationStatus.CONFIRMED)
            .checkInDate(req.checkInDate()).checkOutDate(req.checkOutDate())
            .adults(req.adults()).children(req.children())
            .specialRequests(req.specialRequests())
            .cancellationPolicy(req.cancellationPolicy() != null ? req.cancellationPolicy() : CancellationPolicy.MODERATE)
            .source(req.source() != null ? req.source() : BookingSource.DIRECT)
            .totalAmount(BigDecimal.ZERO).paidAmount(BigDecimal.ZERO)
            .build();

        final Reservation saved = reservationRepository.save(reservation);

        long nights = req.checkInDate().until(req.checkOutDate()).getDays();
        BigDecimal total = BigDecimal.ZERO;
        for (Room room : rooms) {
            RoomType type = room.getRoomType();
            BigDecimal rate = type.getBasePrice();
            ReservationRoom rr = ReservationRoom.builder()
                .reservation(saved).room(room).roomType(type)
                .ratePerNight(rate)
                .checkInDate(req.checkInDate()).checkOutDate(req.checkOutDate())
                .build();
            saved.getReservationRooms().add(rr);
            total = total.add(rate.multiply(BigDecimal.valueOf(nights)));
        }
        saved.setTotalAmount(total);
        return reservationMapper.toResponse(reservationRepository.save(saved));
    }

    @Transactional
    public ReservationResponse confirm(Long id) {
        Reservation r = findReservation(id);
        if (r.getStatus() != ReservationStatus.PENDING)
            throw new BusinessException("Only PENDING reservations can be confirmed");
        r.setStatus(ReservationStatus.CONFIRMED);
        return reservationMapper.toResponse(reservationRepository.save(r));
    }

    @Transactional
    public ReservationResponse checkIn(Long id) {
        Reservation r = findReservation(id);
        if (r.getStatus() != ReservationStatus.CONFIRMED)
            throw new BusinessException("Only CONFIRMED reservations can be checked in");
        r.setStatus(ReservationStatus.CHECKED_IN);
        r.getReservationRooms().forEach(rr -> {
            rr.setActualCheckIn(LocalDateTime.now());
            rr.getRoom().setStatus(Room.RoomStatus.OCCUPIED);
        });
        return reservationMapper.toResponse(reservationRepository.save(r));
    }

    @Transactional
    public ReservationResponse checkOut(Long id) {
        Reservation r = findReservation(id);
        if (r.getStatus() != ReservationStatus.CHECKED_IN)
            throw new BusinessException("Reservation is not checked in");
        r.setStatus(ReservationStatus.CHECKED_OUT);
        r.getReservationRooms().forEach(rr -> {
            rr.setActualCheckOut(LocalDateTime.now());
            rr.getRoom().setStatus(Room.RoomStatus.DIRTY);
        });
        return reservationMapper.toResponse(reservationRepository.save(r));
    }

    @Transactional
    public ReservationResponse cancel(Long id, String reason) {
        Reservation r = findReservation(id);
        if (r.getStatus() == ReservationStatus.CHECKED_IN || r.getStatus() == ReservationStatus.CHECKED_OUT)
            throw new BusinessException("Cannot cancel a reservation that is checked in or already checked out");
        r.setStatus(ReservationStatus.CANCELLED);
        r.setCancelledAt(LocalDateTime.now());
        r.setCancellationReason(reason);
        return reservationMapper.toResponse(reservationRepository.save(r));
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    private String generateConfirmation() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String candidate;
        do {
            StringBuilder sb = new StringBuilder("HM-");
            for (int i = 0; i < 6; i++) sb.append(chars.charAt((int) (Math.random() * chars.length())));
            candidate = sb.toString();
        } while (reservationRepository.findByConfirmationNumberAndDeletedAtIsNull(candidate).isPresent());
        return candidate;
    }
}
