package com.hotel.service;

import com.hotel.dto.request.CreateReservationRequest;
import com.hotel.dto.response.ReservationResponse;
import com.hotel.entity.*;
import com.hotel.exception.BookingConflictException;
import com.hotel.mapper.ReservationMapper;
import com.hotel.repository.*;
import com.hotel.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock RoomRepository roomRepository;
    @Mock RoomTypeRepository roomTypeRepository;
    @Mock GuestRepository guestRepository;
    @Mock HotelRepository hotelRepository;
    @Mock ReservationMapper reservationMapper;

    @InjectMocks ReservationServiceImpl reservationService;

    private Hotel hotel;
    private Guest guest;
    private RoomType roomType;
    private Room room;

    @BeforeEach
    void setUp() {
        // Lombok @Builder excludes BaseEntity.id — set it via setter after build
        hotel = Hotel.builder().name("Test Hotel").build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        guest = Guest.builder().firstName("John").lastName("Doe").email("john@test.com").build();
        ReflectionTestUtils.setField(guest, "id", 1L);

        roomType = RoomType.builder().hotel(hotel).name("Standard")
            .basePrice(BigDecimal.valueOf(100)).maxOccupancy(2).build();
        ReflectionTestUtils.setField(roomType, "id", 1L);

        room = Room.builder().hotel(hotel).roomType(roomType)
            .roomNumber("101").floor(1).status(Room.RoomStatus.AVAILABLE).build();
        ReflectionTestUtils.setField(room, "id", 1L);
    }

    @Test
    void createReservation_withAvailableRoom_succeeds() {
        LocalDate checkIn  = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(guestRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(guest));
        when(roomRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(room));
        when(roomRepository.findAvailableRooms(eq(1L), eq(checkIn), eq(checkOut), isNull()))
            .thenReturn(List.of(room));
        when(reservationRepository.findByConfirmationNumberAndDeletedAtIsNull(any()))
            .thenReturn(Optional.empty());
        when(reservationRepository.save(any())).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 1L);
            return r;
        });
        // Mapper delegates back to the real reservation so totalAmount is visible
        when(reservationMapper.toResponse(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            return buildResponse(r);
        });

        CreateReservationRequest req = new CreateReservationRequest(
            1L, 1L, checkIn, checkOut, 2, 0,
            List.of(new CreateReservationRequest.RoomSelection(1L, 1L)),
            null, null, null);

        ReservationResponse response = reservationService.create(req);

        assertThat(response).isNotNull();
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(200)); // 2 nights × $100
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_withUnavailableRoom_throwsConflict() {
        LocalDate checkIn  = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(guestRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(guest));
        when(roomRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(room));
        when(roomRepository.findAvailableRooms(eq(1L), eq(checkIn), eq(checkOut), isNull()))
            .thenReturn(List.of()); // room unavailable

        CreateReservationRequest req = new CreateReservationRequest(
            1L, 1L, checkIn, checkOut, 2, 0,
            List.of(new CreateReservationRequest.RoomSelection(1L, 1L)),
            null, null, null);

        assertThatThrownBy(() -> reservationService.create(req))
            .isInstanceOf(BookingConflictException.class);
    }

    @Test
    void checkOut_updatesRoomToDirty() {
        Reservation reservation = Reservation.builder()
            .hotel(hotel).guest(guest)
            .confirmationNumber("HM-TEST01")
            .status(Reservation.ReservationStatus.CHECKED_IN)
            .checkInDate(LocalDate.now().minusDays(2))
            .checkOutDate(LocalDate.now())
            .totalAmount(BigDecimal.valueOf(200)).paidAmount(BigDecimal.valueOf(200))
            .build();
        ReflectionTestUtils.setField(reservation, "id", 1L);

        ReservationRoom rr = ReservationRoom.builder()
            .reservation(reservation).room(room).roomType(roomType)
            .ratePerNight(BigDecimal.valueOf(100))
            .checkInDate(LocalDate.now().minusDays(2))
            .checkOutDate(LocalDate.now())
            .build();
        reservation.getReservationRooms().add(rr);

        when(reservationRepository.findByIdAndDeletedAtIsNull(1L))
            .thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenReturn(reservation);
        when(reservationMapper.toResponse(any(Reservation.class))).thenAnswer(inv ->
            buildResponse(inv.getArgument(0)));

        ReservationResponse response = reservationService.checkOut(1L);

        assertThat(response.status()).isEqualTo(Reservation.ReservationStatus.CHECKED_OUT);
        assertThat(room.getStatus()).isEqualTo(Room.RoomStatus.DIRTY);
    }

    // Minimal response builder for stubbing the mapper in unit tests
    private ReservationResponse buildResponse(Reservation r) {
        return new ReservationResponse(
            r.getId(), r.getConfirmationNumber(), r.getStatus(),
            r.getCheckInDate(), r.getCheckOutDate(), r.getAdults(), r.getChildren(),
            r.getSpecialRequests(), r.getCancellationPolicy(),
            r.getCancelledAt(), r.getCancellationReason(),
            r.getTotalAmount(), r.getPaidAmount(), r.getBalanceDue(),
            r.getSource(),
            r.getGuest() != null ? new ReservationResponse.GuestSummary(
                r.getGuest().getId(), r.getGuest().getFirstName(),
                r.getGuest().getLastName(), r.getGuest().getEmail(), r.getGuest().getPhone()) : null,
            r.getHotel() != null ? new ReservationResponse.HotelSummary(
                r.getHotel().getId(), r.getHotel().getName(), r.getHotel().getCity()) : null,
            r.getCreatedAt()
        );
    }
}
