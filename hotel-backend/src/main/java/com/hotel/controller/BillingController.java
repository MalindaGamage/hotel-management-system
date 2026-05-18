package com.hotel.controller;

import com.hotel.dto.request.ProcessPaymentRequest;
import com.hotel.dto.response.PagedResponse;
import com.hotel.entity.Invoice;
import com.hotel.entity.Payment;
import com.hotel.service.impl.BillingServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Billing", description = "Invoices and payment processing")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingServiceImpl billingService;

    @GetMapping("/invoices")
    @PreAuthorize("hasAuthority('billing:read')")
    public ResponseEntity<PagedResponse<Invoice>> getInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(billingService.getInvoices(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAuthority('billing:read')")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getInvoiceById(id));
    }

    @Operation(summary = "Download invoice as PDF")
    @GetMapping("/invoices/{id}/pdf")
    @PreAuthorize("hasAuthority('billing:read')")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        byte[] pdf = billingService.generateInvoicePdf(id);
        Invoice inv = billingService.getInvoiceById(id);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename(inv.getInvoiceNumber() + ".pdf")
                    .build().toString())
            .body(pdf);
    }

    @Operation(summary = "Generate an invoice for a reservation")
    @PostMapping("/invoices")
    @PreAuthorize("hasAuthority('billing:write')")
    public ResponseEntity<Invoice> createInvoice(@RequestBody Map<String, Long> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(billingService.createInvoice(body.get("reservationId")));
    }

    @Operation(summary = "Issue a draft invoice")
    @PatchMapping("/invoices/{id}/issue")
    @PreAuthorize("hasAuthority('billing:write')")
    public ResponseEntity<Invoice> issueInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.issueInvoice(id));
    }

    @Operation(summary = "Process a payment — include X-Idempotency-Key header to prevent duplicate charges")
    @PostMapping("/payments")
    @PreAuthorize("hasAuthority('billing:write')")
    public ResponseEntity<Payment> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        String key = (idempotencyKey != null && !idempotencyKey.isBlank())
            ? idempotencyKey : UUID.randomUUID().toString();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(billingService.processPayment(request, key));
    }
}
