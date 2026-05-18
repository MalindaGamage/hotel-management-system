package com.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @NotBlank
    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @DecimalMin("0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.TEN;

    @DecimalMin("0.00")
    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    public void recalculateTotals() {
        this.subtotal = items.stream()
            .filter(i -> i.getItemType() != InvoiceItem.ItemType.DISCOUNT && i.getItemType() != InvoiceItem.ItemType.TAX)
            .map(InvoiceItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100));
        this.totalAmount = subtotal.add(taxAmount);
    }

    public enum InvoiceStatus {
        DRAFT, ISSUED, PAID, CANCELLED
    }
}
