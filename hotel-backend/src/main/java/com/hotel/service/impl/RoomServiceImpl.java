package com.hotel.service.impl;

import com.hotel.dto.request.CreateRoomRequest;
import com.hotel.dto.request.CreateRoomTypeRequest;
import com.hotel.dto.request.UpdateRoomStatusRequest;
import com.hotel.dto.response.PagedResponse;
import com.hotel.dto.response.RoomResponse;
import com.hotel.entity.Hotel;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.exception.ConflictException;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.mapper.RoomMapper;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;

    public PagedResponse<RoomResponse> getRooms(Long hotelId, Pageable pageable) {
        return PagedResponse.of(
            roomRepository.findAllByHotelIdAndDeletedAtIsNull(hotelId, pageable).map(roomMapper::toResponse));
    }

    public RoomResponse getRoomById(Long id) {
        return roomMapper.toResponse(findRoom(id));
    }

    public List<RoomResponse> getAvailableRooms(Long hotelId, LocalDate checkIn, LocalDate checkOut, Long roomTypeId) {
        return roomRepository.findAvailableRooms(hotelId, checkIn, checkOut, roomTypeId)
            .stream().map(roomMapper::toResponse).toList();
    }

    @Transactional
    public RoomResponse createRoom(CreateRoomRequest req) {
        if (roomRepository.existsByHotelIdAndRoomNumberAndDeletedAtIsNull(req.hotelId(), req.roomNumber())) {
            throw new ConflictException("Room number " + req.roomNumber() + " already exists in this hotel");
        }
        Hotel hotel = hotelRepository.findById(req.hotelId())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel", req.hotelId()));
        RoomType type = roomTypeRepository.findByIdAndDeletedAtIsNull(req.roomTypeId())
            .orElseThrow(() -> new ResourceNotFoundException("RoomType", req.roomTypeId()));

        Room room = roomMapper.toEntity(req);
        room.setHotel(hotel);
        room.setRoomType(type);
        room.setStatus(Room.RoomStatus.AVAILABLE);
        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse updateRoomStatus(Long id, UpdateRoomStatusRequest req) {
        Room room = findRoom(id);
        room.setStatus(req.status());
        if (req.notes() != null) room.setNotes(req.notes());
        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = findRoom(id);
        room.softDelete();
        roomRepository.save(room);
    }

    public PagedResponse<RoomResponse> getRoomTypes(Long hotelId, Pageable pageable) {
        return PagedResponse.of(
            roomTypeRepository.findAllByHotelIdAndDeletedAtIsNull(hotelId, pageable)
                .map(roomMapper::toRoomTypeSummary)
                .map(summary -> new RoomResponse(null, null, 0, null, null, hotelId, summary)));
    }

    @Transactional
    public RoomResponse createRoomType(CreateRoomTypeRequest req) {
        if (roomTypeRepository.existsByHotelIdAndNameAndDeletedAtIsNull(req.hotelId(), req.name())) {
            throw new ConflictException("Room type '" + req.name() + "' already exists in this hotel");
        }
        Hotel hotel = hotelRepository.findById(req.hotelId())
            .orElseThrow(() -> new ResourceNotFoundException("Hotel", req.hotelId()));

        RoomType type = roomMapper.roomTypeToEntity(req);
        type.setHotel(hotel);
        roomTypeRepository.save(type);
        return new RoomResponse(null, null, 0, null, null, req.hotelId(),
            roomMapper.toRoomTypeSummary(type));
    }

    private Room findRoom(Long id) {
        return roomRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }
}
