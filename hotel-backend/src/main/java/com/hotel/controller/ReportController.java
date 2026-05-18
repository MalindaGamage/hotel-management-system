package com.hotel.controller;

import com.hotel.service.impl.ReportServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "Reports", description = "Analytics, KPIs, and export")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportServiceImpl reportService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('reports:read')")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestParam Long hotelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportService.getDashboardKpis(hotelId, date != null ? date : LocalDate.now()));
    }

    @Operation(summary = "Occupancy and revenue report for a date range")
    @GetMapping("/occupancy")
    @PreAuthorize("hasAuthority('reports:read')")
    public ResponseEntity<Map<String, Object>> getOccupancyReport(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getOccupancyReport(hotelId, from, to));
    }

    @Operation(summary = "Download occupancy report as Excel (.xlsx)")
    @GetMapping("/occupancy/export")
    @PreAuthorize("hasAuthority('reports:read')")
    public ResponseEntity<byte[]> exportOccupancyExcel(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        byte[] data = reportService.exportOccupancyReportExcel(hotelId, from, to);
        String filename = String.format("occupancy_%s_%s.xlsx", from, to);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(filename).build().toString())
            .body(data);
    }
}
