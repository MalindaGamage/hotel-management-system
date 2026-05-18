package com.hotel.mapper;

import com.hotel.dto.request.CreateGuestRequest;
import com.hotel.dto.response.GuestResponse;
import com.hotel.entity.Guest;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GuestMapper {

    GuestResponse toResponse(Guest guest);

    // BaseEntity fields (id, createdAt, updatedAt, deletedAt) are not in the Lombok
    // builder for subclasses — rely on IGNORE policy to skip them automatically.
    @Mapping(target = "loyaltyPoints", ignore = true)
    @Mapping(target = "loyaltyTier",   ignore = true)
    @Mapping(target = "reservations",  ignore = true)
    Guest toEntity(CreateGuestRequest request);

    // For updates: entity has setters for all fields including BaseEntity ones.
    // Boolean field "isVerified" → Lombok generates setVerified(), so target is "verified".
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "loyaltyPoints", ignore = true)
    @Mapping(target = "loyaltyTier",   ignore = true)
    @Mapping(target = "verified",      ignore = true)
    @Mapping(target = "reservations",  ignore = true)
    @Mapping(target = "id",            ignore = true)
    @Mapping(target = "createdAt",     ignore = true)
    @Mapping(target = "updatedAt",     ignore = true)
    @Mapping(target = "deletedAt",     ignore = true)
    void updateEntityFromRequest(CreateGuestRequest request, @MappingTarget Guest guest);
}
