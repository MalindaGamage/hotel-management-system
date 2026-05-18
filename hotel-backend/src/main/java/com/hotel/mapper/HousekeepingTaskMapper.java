package com.hotel.mapper;

import com.hotel.dto.response.HousekeepingTaskResponse;
import com.hotel.entity.HousekeepingTask;
import com.hotel.entity.Room;
import com.hotel.entity.Staff;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HousekeepingTaskMapper {

    @Mapping(target = "room",        source = "room")
    @Mapping(target = "assignedTo",  source = "assignedTo")
    HousekeepingTaskResponse toResponse(HousekeepingTask task);

    @Mapping(target = "id",         source = "id")
    @Mapping(target = "roomNumber", source = "roomNumber")
    @Mapping(target = "floor",      source = "floor")
    @Mapping(target = "status",     source = "status")
    HousekeepingTaskResponse.RoomSummary toRoomSummary(Room room);

    @Mapping(target = "id",        source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName",  source = "lastName")
    HousekeepingTaskResponse.StaffSummary toStaffSummary(Staff staff);
}
