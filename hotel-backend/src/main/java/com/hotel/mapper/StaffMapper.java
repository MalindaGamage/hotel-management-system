package com.hotel.mapper;

import com.hotel.dto.response.StaffSummary;
import com.hotel.entity.Staff;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StaffMapper {

    @Mapping(target = "role", source = "role.name")
    StaffSummary toSummary(Staff staff);
}
