package com.nativetravel.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.nativetravel.entity.Booking;
import com.nativetravel.service.PdfVoucherService;
import com.nativetravel.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Реализация сервиса создания PDF ваучеров на базе библиотеки OpenPDF.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfVoucherServiceImpl implements PdfVoucherService {

    private final QrCodeService qrCodeService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    @Override
    public byte[] generateBookingVoucherPdf(Booking booking) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(13, 71, 161));
            Font subTitleFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(80, 80, 80));
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font regularFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);

            // Document Header
            Paragraph brand = new Paragraph("NATIVE TRAVEL AGENCY", titleFont);
            brand.setAlignment(Element.ALIGN_CENTER);
            document.add(brand);

            Paragraph title = new Paragraph("TRAVEL VOUCHER / BOOKING CONFIRMATION", subTitleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            // Top Info Table: Voucher Details & QR Code
            PdfPTable topTable = new PdfPTable(2);
            topTable.setWidthPercentage(100);
            topTable.setWidths(new float[]{70, 30});
            topTable.setSpacingAfter(15);

            // Left Cell: Booking Details
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(PdfPCell.NO_BORDER);
            leftCell.addElement(new Paragraph("Booking Number: " + booking.getBookingNumber(), boldFont));
            leftCell.addElement(new Paragraph("Date of Issue: " + DATE_FORMATTER.format(booking.getCreatedAt()), regularFont));
            leftCell.addElement(new Paragraph("Status: " + booking.getStatus().name() + " (CONFIRMED)", boldFont));
            leftCell.addElement(new Paragraph("Client: " + (booking.getUser() != null ? booking.getUser().getFullName() : "N/A"), regularFont));
            leftCell.addElement(new Paragraph("Email: " + (booking.getUser() != null ? booking.getUser().getEmail() : "N/A"), regularFont));
            topTable.addCell(leftCell);

            // Right Cell: QR Code
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(PdfPCell.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            String qrContent = "NATIVE-TRAVEL:" + booking.getBookingNumber() + ":" + booking.getTotalPrice() + "RUB";
            byte[] qrBytes = qrCodeService.generateQrCodePng(qrContent, 120, 120);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.scaleToFit(110, 110);
            rightCell.addElement(qrImage);
            topTable.addCell(rightCell);

            document.add(topTable);

            // Item Details Table
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setWidths(new float[]{35, 65});
            detailsTable.setSpacingAfter(15);

            PdfPCell tableHeader = new PdfPCell(new Phrase("ORDER DETAILS / ITINERARY", headerFont));
            tableHeader.setColspan(2);
            tableHeader.setBackgroundColor(new Color(25, 118, 210));
            tableHeader.setPadding(6);
            detailsTable.addCell(tableHeader);

            if (booking.getTour() != null) {
                addTableRow(detailsTable, "Item Type", "TOUR PACKAGE", regularFont, boldFont);
                addTableRow(detailsTable, "Tour Title", booking.getTour().getTitle(), regularFont, boldFont);
                addTableRow(detailsTable, "Destination", booking.getTour().getCountry() + ", " + booking.getTour().getCity(), regularFont, regularFont);
                if (booking.getTour().getHotel() != null) {
                    addTableRow(detailsTable, "Hotel", booking.getTour().getHotel().getName() + " (" + booking.getTour().getHotel().getStars() + " Stars)", regularFont, regularFont);
                }
                addTableRow(detailsTable, "Dates", booking.getTour().getStartDate() + " - " + booking.getTour().getEndDate() + " (" + booking.getTour().getDurationDays() + " days)", regularFont, regularFont);
                addTableRow(detailsTable, "Services Included", booking.getTour().getIncludedServices() != null ? booking.getTour().getIncludedServices() : "Standard package", regularFont, regularFont);
            } else if (booking.getFlight() != null) {
                addTableRow(detailsTable, "Item Type", "FLIGHT TICKET", regularFont, boldFont);
                addTableRow(detailsTable, "Flight Number", booking.getFlight().getFlightNumber() + " (" + booking.getFlight().getAirline() + ")", regularFont, boldFont);
                addTableRow(detailsTable, "Route", booking.getFlight().getDepartureCity() + " -> " + booking.getFlight().getArrivalCity(), regularFont, regularFont);
                addTableRow(detailsTable, "Departure Time", DATE_FORMATTER.format(booking.getFlight().getDepartureTime()), regularFont, regularFont);
                addTableRow(detailsTable, "Arrival Time", DATE_FORMATTER.format(booking.getFlight().getArrivalTime()), regularFont, regularFont);
            }

            document.add(detailsTable);

            // Payment Summary Table
            PdfPTable paymentTable = new PdfPTable(2);
            paymentTable.setWidthPercentage(100);
            paymentTable.setWidths(new float[]{50, 50});
            paymentTable.setSpacingAfter(20);

            PdfPCell payHeader = new PdfPCell(new Phrase("PAYMENT SUMMARY", headerFont));
            payHeader.setColspan(2);
            payHeader.setBackgroundColor(new Color(56, 142, 60));
            payHeader.setPadding(6);
            paymentTable.addCell(payHeader);

            addTableRow(paymentTable, "Original Price:", booking.getOriginalPrice() + " RUB", regularFont, regularFont);
            addTableRow(paymentTable, "Promo Discount:", "-" + booking.getDiscountAmount() + " RUB", regularFont, regularFont);
            addTableRow(paymentTable, "Total Amount Paid:", booking.getTotalPrice() + " RUB", boldFont, new Font(Font.HELVETICA, 11, Font.BOLD, new Color(27, 94, 32)));
            addTableRow(paymentTable, "Payment Method:", booking.getPaymentMethod() != null ? booking.getPaymentMethod().name() : "PAID", regularFont, regularFont);

            document.add(paymentTable);

            // Footer note
            Paragraph footer = new Paragraph("Important: Present this voucher and QR code upon check-in at the airport/hotel. " +
                    "For 24/7 customer support contact: support@nativetravel.local | +7 (800) 555-35-35", 
                    new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF voucher for booking: {}", booking.getBookingNumber(), e);
            throw new RuntimeException("Ошибка создания PDF-ваучера", e);
        }
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setPadding(5);
        c1.setBackgroundColor(new Color(245, 245, 245));
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "—", valFont));
        c2.setPadding(5);
        table.addCell(c2);
    }
}

