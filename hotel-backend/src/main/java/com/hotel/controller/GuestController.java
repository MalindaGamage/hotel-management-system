package com.hotel.controller;

import com.hotel.dto.request.CreateGuestRequest;
import com.hotel.dto.response.GuestResponse;
import com.hotel.dto.response.PagedResponse;
import com.hotel.service.impl.GuestServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Guests", description = "Guest profile management and loyalty")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/v1/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestServiceImpl guestService;

    @GetMapping
    @PreAuthorize("hasAuthority('guests:read')")
    public ResponseEntity<PagedResponse<GuestResponse>> getGuests(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(guestService.getGuests(search,
            PageRequest.of(page, size, Sort.by("lastName"))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('guests:read')")
    public ResponseEntity<GuestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(guestService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('guests:write')")
    public ResponseEntity<GuestResponse> create(@Valid @RequestBody CreateGuestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guestService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('guests:write')")
    public ResponseEntity<GuestResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody CreateGuestRequest request) {
        return ResponseEntity.ok(guestService.update(id, request));
    }

    @Operation(summary = "Add loyalty points to a guest")
    @PatchMapping("/{id}/loyalty-points")
    @PreAuthorize("hasAuthority('guests:write')")
    public ResponseEntity<Void> addLoyaltyPoints(@PathVariable Long id,
                                                   @RequestBody Map<String, Integer> body) {
        guestService.addLoyaltyPoints(id, body.get("points"));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('rooms:delete')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        guestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
