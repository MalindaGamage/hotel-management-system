package com.hotel.mapper;

import com.hotel.dto.response.ReservationResponse;
import com.hotel.entity.Guest;
import com.hotel.entity.Hotel;
import com.hotel.entity.Reservation;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationMapper {

    @Mapping(target = "guest",      source = "guest")
    @Mapping(target = "hotel",      source = "hotel")
    @Mapping(target = "balanceDue", expression = "java(reservation.getBalanceDue())")
    ReservationResponse toResponse(Reservation reservation);

    @Mapping(target = "id",        source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName",  source = "lastName")
    @Mapping(target = "email",     source = "email")
    @Mapping(target = "phone",     source = "phone")
    ReservationResponse.GuestSummary toGuestSummary(Guest guest);

    @Mapping(target = "id",   source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "city", source = "city")
    ReservationResponse.HotelSummary toHotelSummary(Hotel hotel);
}
