package com.hookah.kek_hookah.feature.reports.internal.usecase

import com.hookah.kek_hookah.feature.reports.internal.repository.ReportsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream

@Component
class StockReportUseCase(
    private val repository: ReportsRepository,
) {

    suspend fun generateXlsx(): ByteArray {
        val rows = repository.stockReport().toList()

        return withContext(Dispatchers.IO) {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Остатки")

            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Название")
            headerRow.createCell(1).setCellValue("Вес (г)")

            rows.forEachIndexed { index, row ->
                val dataRow = sheet.createRow(index + 1)
                dataRow.createCell(0).setCellValue("${row.brandName} ${row.flavorName}")
                dataRow.createCell(1).setCellValue(row.weightGrams.toDouble())
            }

            sheet.setColumnWidth(0, 10000)
            sheet.setColumnWidth(1, 4000)

            val out = ByteArrayOutputStream()
            workbook.write(out)
            workbook.close()
            out.toByteArray()
        }
    }
}
