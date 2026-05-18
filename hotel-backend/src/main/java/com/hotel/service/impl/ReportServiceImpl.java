package com.hotel.service.impl;

import com.hotel.entity.Room.RoomStatus;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public Map<String, Object> getDashboardKpis(Long hotelId, LocalDate today) {
        long totalRooms  = roomRepository.countByHotelId(hotelId);
        long arrivals    = reservationRepository.countArrivalsForDate(hotelId, today);
        long departures  = reservationRepository.countDeparturesForDate(hotelId, today);
        long occupied    = roomRepository.countByHotelIdAndStatus(hotelId, RoomStatus.OCCUPIED);
        BigDecimal monthRevenue = reservationRepository.sumRevenueByDateRange(
            hotelId, today.withDayOfMonth(1), today);

        double occupancyPct = totalRooms > 0
            ? BigDecimal.valueOf(occupied * 100.0 / totalRooms).setScale(1, RoundingMode.HALF_UP).doubleValue()
            : 0.0;

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalRooms",       totalRooms);
        kpis.put("occupiedRooms",    occupied);
        kpis.put("occupancyPct",     occupancyPct);
        kpis.put("arrivalsToday",    arrivals);
        kpis.put("departuresToday",  departures);
        kpis.put("revenueMonthToDate", monthRevenue);
        return kpis;
    }

    public Map<String, Object> getOccupancyReport(Long hotelId, LocalDate from, LocalDate to) {
        long totalRooms = roomRepository.countByHotelId(hotelId);
        BigDecimal revenue = reservationRepository.sumRevenueByDateRange(hotelId, from, to);
        List<Object[]> daily = reservationRepository.findDailyRevenue(hotelId, from, to);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("hotelId",    hotelId);
        report.put("from",       from.toString());
        report.put("to",         to.toString());
        report.put("totalRooms", totalRooms);
        report.put("totalRevenue", revenue);
        report.put("dailyBreakdown", daily.stream().map(row -> Map.of(
            "date",         row[0].toString(),
            "reservations", row[1],
            "revenue",      row[2]
        )).toList());
        return report;
    }

    public byte[] exportOccupancyReportExcel(Long hotelId, LocalDate from, LocalDate to) {
        List<Object[]> daily = reservationRepository.findDailyRevenue(hotelId, from, to);
        long totalRooms = roomRepository.countByHotelId(hotelId);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Occupancy Report");
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 4000);
            sheet.setColumnWidth(2, 5000);
            sheet.setColumnWidth(3, 4000);

            // Header styles
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat df = workbook.createDataFormat();
            currencyStyle.setDataFormat(df.getFormat("#,##0.00"));

            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Occupancy & Revenue Report — Hotel " + hotelId);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            // Date range row
            Row rangeRow = sheet.createRow(1);
            rangeRow.createCell(0).setCellValue("Period: " + from + " to " + to);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

            // Column headers
            Row headers = sheet.createRow(3);
            String[] cols = {"Date", "Total Rooms", "Reservations", "Revenue ($)"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = headers.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 4;
            BigDecimal grandTotal = BigDecimal.ZERO;
            for (Object[] row : daily) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(row[0].toString());
                dataRow.createCell(1).setCellValue(totalRooms);
                dataRow.createCell(2).setCellValue(((Number) row[1]).longValue());
                Cell revCell = dataRow.createCell(3);
                BigDecimal rev = new BigDecimal(row[2].toString());
                revCell.setCellValue(rev.doubleValue());
                revCell.setCellStyle(currencyStyle);
                grandTotal = grandTotal.add(rev);
            }

            // Total row
            Row totalRow = sheet.createRow(rowNum + 1);
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            CellStyle totalStyle = workbook.createCellStyle();
            totalStyle.setFont(boldFont);
            totalStyle.setDataFormat(df.getFormat("#,##0.00"));
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TOTAL");
            totalLabel.setCellStyle(totalStyle);
            Cell totalCell = totalRow.createCell(3);
            totalCell.setCellValue(grandTotal.doubleValue());
            totalCell.setCellStyle(totalStyle);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
}
