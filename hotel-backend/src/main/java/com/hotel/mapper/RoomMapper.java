package com.hotel.mapper;

import com.hotel.dto.request.CreateRoomRequest;
import com.hotel.dto.request.CreateRoomTypeRequest;
import com.hotel.dto.response.RoomResponse;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoomMapper {

    @Mapping(target = "hotelId",  source = "hotel.id")
    @Mapping(target = "roomType", source = "roomType")
    RoomResponse toResponse(Room room);

    RoomResponse.RoomTypeSummary toRoomTypeSummary(RoomType roomType);

    // BaseEntity fields are not part of the Lombok builder for subclasses —
    // IGNORE policy skips them. Explicit ignores only needed for fields MapStruct
    // would otherwise try to map from the request.
    @Mapping(target = "hotel",    ignore = true)
    @Mapping(target = "roomType", ignore = true)
    @Mapping(target = "status",   ignore = true)
    @Mapping(target = "version",  ignore = true)
    Room toEntity(CreateRoomRequest request);

    @Mapping(target = "hotel",  ignore = true)
    @Mapping(target = "rooms",  ignore = true)
    RoomType roomTypeToEntity(CreateRoomTypeRequest request);
}
