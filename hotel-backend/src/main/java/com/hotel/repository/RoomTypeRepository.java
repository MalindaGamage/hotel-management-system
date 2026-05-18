package com.hotel.repository;

import com.hotel.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    List<RoomType> findAllByHotelIdAndDeletedAtIsNull(Long hotelId);

    Page<RoomType> findAllByHotelIdAndDeletedAtIsNull(Long hotelId, Pageable pageable);

    Optional<RoomType> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByHotelIdAndNameAndDeletedAtIsNull(Long hotelId, String name);
}
