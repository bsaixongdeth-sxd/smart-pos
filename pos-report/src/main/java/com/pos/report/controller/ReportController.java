package com.pos.report.controller;

import com.pos.common.dto.ApiResponse;
import com.pos.report.dto.*;
import com.pos.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Reports")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Sales report for date range")
    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<SalesReportDto>> sales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.salesReport(from, to)));
    }

    @Operation(summary = "Daily close summary")
    @GetMapping("/daily-close")
    public ResponseEntity<ApiResponse<DailyCloseDto>> dailyClose(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(reportService.dailyClose(date)));
    }

    @Operation(summary = "Top products by quantity sold")
    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<List<TopProductDto>>> topProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(reportService.topProducts(limit)));
    }

    @Operation(summary = "Cashier performance summary")
    @GetMapping("/cashier-summary")
    public ResponseEntity<ApiResponse<CashierSummaryDto>> cashierSummary(
            @RequestParam UUID cashierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(reportService.cashierSummary(cashierId, date)));
    }

    @Operation(summary = "Export orders as CSV")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        String csv = reportService.exportCsv(from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"sales_" + from + "_" + to + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }
}
