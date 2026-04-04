package com.hookah.kek_hookah.feature.reports.api

import com.hookah.kek_hookah.feature.reports.internal.usecase.StockReportUseCase
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reports")
class ReportsController(
    private val stockReportUseCase: StockReportUseCase,
) {

    @GetMapping("/stock")
    suspend fun downloadStock(): ResponseEntity<ByteArray> {
        val bytes = stockReportUseCase.generateXlsx()
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("ostatok.xlsx").build().toString()
            )
            .body(bytes)
    }
}
