package com.hotel.service.impl;

import com.hotel.dto.request.CreateGuestRequest;
import com.hotel.dto.response.GuestResponse;
import com.hotel.dto.response.PagedResponse;
import com.hotel.entity.Guest;
import com.hotel.exception.ConflictException;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.mapper.GuestMapper;
import com.hotel.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestServiceImpl {

    private final GuestRepository guestRepository;
    private final GuestMapper guestMapper;

    public PagedResponse<GuestResponse> getGuests(String search, Pageable pageable) {
        return PagedResponse.of(guestRepository.searchGuests(search, pageable).map(guestMapper::toResponse));
    }

    public GuestResponse getById(Long id) {
        return guestMapper.toResponse(find(id));
    }

    @Transactional
    public GuestResponse create(CreateGuestRequest req) {
        if (guestRepository.existsByEmailAndDeletedAtIsNull(req.email())) {
            throw new ConflictException("Guest with email " + req.email() + " already exists");
        }
        Guest g = guestMapper.toEntity(req);
        return guestMapper.toResponse(guestRepository.save(g));
    }

    @Transactional
    public GuestResponse update(Long id, CreateGuestRequest req) {
        Guest g = find(id);
        guestMapper.updateEntityFromRequest(req, g);
        return guestMapper.toResponse(guestRepository.save(g));
    }

    @Transactional
    public void addLoyaltyPoints(Long id, int points) {
        Guest g = find(id);
        g.setLoyaltyPoints(g.getLoyaltyPoints() + points);
        g.recalculateLoyaltyTier();
        guestRepository.save(g);
    }

    @Transactional
    public void delete(Long id) {
        Guest g = find(id);
        g.softDelete();
        guestRepository.save(g);
    }

    private Guest find(Long id) {
        return guestRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Guest", id));
    }
}
