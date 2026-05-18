package com.hotel.service.impl;

import com.hotel.dto.request.ProcessPaymentRequest;
import com.hotel.dto.response.PagedResponse;
import com.hotel.entity.*;
import com.hotel.entity.Invoice.InvoiceStatus;
import com.hotel.entity.Payment.PaymentStatus;
import com.hotel.exception.*;
import com.hotel.repository.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillingServiceImpl {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final Font TITLE_FONT  = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
    private static final Font BODY_FONT   = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font BOLD_FONT   = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);

    public PagedResponse<Invoice> getInvoices(Pageable pageable) {
        return PagedResponse.of(invoiceRepository.findAllByDeletedAtIsNull(pageable));
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    @Transactional
    public Invoice createInvoice(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndDeletedAtIsNull(reservationId)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));

        Invoice invoice = Invoice.builder()
            .reservation(reservation).guest(reservation.getGuest())
            .invoiceNumber(generateInvoiceNumber())
            .status(InvoiceStatus.DRAFT)
            .taxRate(BigDecimal.TEN)
            .subtotal(BigDecimal.ZERO).taxAmount(BigDecimal.ZERO).totalAmount(BigDecimal.ZERO)
            .build();

        reservation.getReservationRooms().forEach(rr -> {
            InvoiceItem item = InvoiceItem.builder()
                .invoice(invoice)
                .description(String.format("%s — %d night(s) (%s to %s)",
                    rr.getRoomType().getName(), rr.getNights(),
                    rr.getCheckInDate(), rr.getCheckOutDate()))
                .quantity((int) rr.getNights())
                .unitPrice(rr.getRatePerNight())
                .totalPrice(rr.getTotalAmount())
                .itemType(InvoiceItem.ItemType.ROOM)
                .build();
            invoice.getItems().add(item);
        });

        invoice.recalculateTotals();
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice issueInvoice(Long id) {
        Invoice inv = getInvoiceById(id);
        if (inv.getStatus() != InvoiceStatus.DRAFT)
            throw new BusinessException("Invoice is not in DRAFT status");
        inv.setStatus(InvoiceStatus.ISSUED);
        inv.setIssuedAt(LocalDateTime.now());
        inv.setDueAt(LocalDateTime.now().plusDays(7));
        return invoiceRepository.save(inv);
    }

    @Transactional
    public Payment processPayment(ProcessPaymentRequest req, String idempotencyKey) {
        if (paymentRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new IdempotencyConflictException(idempotencyKey);
        }
        Reservation reservation = reservationRepository.findByIdAndDeletedAtIsNull(req.reservationId())
            .orElseThrow(() -> new ResourceNotFoundException("Reservation", req.reservationId()));

        Payment payment = Payment.builder()
            .reservation(reservation).amount(req.amount())
            .paymentMethod(req.paymentMethod())
            .paymentStatus(PaymentStatus.COMPLETED)
            .transactionId(req.transactionId())
            .idempotencyKey(idempotencyKey)
            .paidAt(LocalDateTime.now()).notes(req.notes())
            .build();

        Payment saved = paymentRepository.save(payment);

        BigDecimal totalPaid = paymentRepository.sumCompletedPaymentsByReservation(req.reservationId());
        reservation.setPaidAmount(totalPaid);
        if (totalPaid.compareTo(reservation.getTotalAmount()) >= 0) {
            invoiceRepository.findAllByReservationIdAndDeletedAtIsNull(req.reservationId())
                .forEach(inv -> {
                    if (inv.getStatus() == InvoiceStatus.ISSUED) {
                        inv.setStatus(InvoiceStatus.PAID);
                        inv.setPaidAt(LocalDateTime.now());
                        invoiceRepository.save(inv);
                    }
                });
        }
        reservationRepository.save(reservation);
        return saved;
    }

    public byte[] generateInvoicePdf(Long invoiceId) {
        Invoice inv = getInvoiceById(invoiceId);
        Guest guest = inv.getGuest();
        Reservation res = inv.getReservation();

        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Header
            Paragraph title = new Paragraph("INVOICE", TITLE_FONT);
            title.setAlignment(Element.ALIGN_RIGHT);
            doc.add(title);

            Paragraph invNumber = new Paragraph(inv.getInvoiceNumber(), BOLD_FONT);
            invNumber.setAlignment(Element.ALIGN_RIGHT);
            doc.add(invNumber);

            if (inv.getIssuedAt() != null) {
                Paragraph issuedDate = new Paragraph("Issued: " + inv.getIssuedAt().format(DATE_FMT), BODY_FONT);
                issuedDate.setAlignment(Element.ALIGN_RIGHT);
                doc.add(issuedDate);
            }
            if (inv.getDueAt() != null) {
                Paragraph dueDate = new Paragraph("Due: " + inv.getDueAt().format(DATE_FMT), BODY_FONT);
                dueDate.setAlignment(Element.ALIGN_RIGHT);
                doc.add(dueDate);
            }

            doc.add(Chunk.NEWLINE);

            // Guest info
            doc.add(new Paragraph("Billed To:", BOLD_FONT));
            doc.add(new Paragraph(guest.getFirstName() + " " + guest.getLastName(), BODY_FONT));
            doc.add(new Paragraph(guest.getEmail(), BODY_FONT));
            if (guest.getPhone() != null) doc.add(new Paragraph(guest.getPhone(), BODY_FONT));

            doc.add(Chunk.NEWLINE);

            // Reservation info
            doc.add(new Paragraph("Reservation: " + res.getConfirmationNumber(), BOLD_FONT));
            doc.add(new Paragraph(
                "Check-in: " + res.getCheckInDate() + "   Check-out: " + res.getCheckOutDate(), BODY_FONT));

            doc.add(Chunk.NEWLINE);

            // Line items table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{5f, 1.5f, 2f, 2f});

            addTableHeader(table, "Description");
            addTableHeader(table, "Qty");
            addTableHeader(table, "Unit Price");
            addTableHeader(table, "Total");

            for (InvoiceItem item : inv.getItems()) {
                table.addCell(new PdfPCell(new Phrase(item.getDescription(), BODY_FONT)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), BODY_FONT)));
                table.addCell(new PdfPCell(new Phrase("$" + item.getUnitPrice(), BODY_FONT)));
                table.addCell(new PdfPCell(new Phrase("$" + item.getTotalPrice(), BODY_FONT)));
            }
            doc.add(table);

            doc.add(Chunk.NEWLINE);

            // Totals
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(40);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            addTotalRow(totalsTable, "Subtotal",  "$" + inv.getSubtotal());
            addTotalRow(totalsTable, "Tax (" + inv.getTaxRate() + "%)", "$" + inv.getTaxAmount());
            addTotalRow(totalsTable, "TOTAL",     "$" + inv.getTotalAmount());
            doc.add(totalsTable);

            // Status watermark-style footer
            if (inv.getStatus() == InvoiceStatus.PAID) {
                Paragraph paid = new Paragraph("\nPAID",
                    new Font(Font.FontFamily.HELVETICA, 28, Font.BOLD, new BaseColor(0, 150, 0)));
                paid.setAlignment(Element.ALIGN_CENTER);
                doc.add(paid);
            }

            doc.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new BaseColor(26, 86, 219));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingRight(10);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, BODY_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private String generateInvoiceNumber() {
        int seq = invoiceRepository.findMaxInvoiceSequence() + 1;
        return String.format("INV-%d-%04d", LocalDateTime.now().getYear(), seq);
    }
}
