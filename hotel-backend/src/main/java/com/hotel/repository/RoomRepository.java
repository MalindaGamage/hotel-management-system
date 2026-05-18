package com.hotel.repository;

import com.hotel.entity.Room;
import com.hotel.entity.Room.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByIdAndDeletedAtIsNull(Long id);

    Page<Room> findAllByHotelIdAndDeletedAtIsNull(Long hotelId, Pageable pageable);

    List<Room> findAllByHotelIdAndStatusAndDeletedAtIsNull(Long hotelId, RoomStatus status);

    boolean existsByHotelIdAndRoomNumberAndDeletedAtIsNull(Long hotelId, String roomNumber);

    @Query("""
        SELECT r FROM Room r
        WHERE r.hotel.id      = :hotelId
          AND r.deletedAt     IS NULL
          AND r.status        NOT IN ('OUT_OF_ORDER','MAINTENANCE')
          AND (:roomTypeId    IS NULL OR r.roomType.id = :roomTypeId)
          AND r.id NOT IN (
              SELECT rr.room.id FROM ReservationRoom rr
              JOIN rr.reservation res
              WHERE res.status  NOT IN ('CANCELLED','NO_SHOW')
                AND res.deletedAt IS NULL
                AND rr.checkInDate  < :checkOut
                AND rr.checkOutDate > :checkIn
          )
        """)
    List<Room> findAvailableRooms(
        @Param("hotelId")    Long hotelId,
        @Param("checkIn")    LocalDate checkIn,
        @Param("checkOut")   LocalDate checkOut,
        @Param("roomTypeId") Long roomTypeId
    );

    @Query("SELECT COUNT(r) FROM Room r WHERE r.hotel.id = :hotelId AND r.status = :status AND r.deletedAt IS NULL")
    long countByHotelIdAndStatus(@Param("hotelId") Long hotelId, @Param("status") RoomStatus status);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.hotel.id = :hotelId AND r.deletedAt IS NULL")
    long countByHotelId(@Param("hotelId") Long hotelId);
}
