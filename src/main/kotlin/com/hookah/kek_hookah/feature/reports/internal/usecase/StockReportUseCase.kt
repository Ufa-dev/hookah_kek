package com.hookah.kek_hookah.feature.reports.internal.usecase

import com.hookah.kek_hookah.feature.reports.internal.repository.ReportsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.dhatim.fastexcel.Workbook
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream

@Component
class StockReportUseCase(
    private val repository: ReportsRepository,
) {

    suspend fun generateXlsx(): ByteArray {
        val rows = repository.stockReport().toList()

        return withContext(Dispatchers.IO) {
            val out = ByteArrayOutputStream()
            Workbook(out, "hookah-stock", "1.0").use { wb ->
                val ws = wb.newWorksheet("Остатки")
                ws.value(0, 0, "Название")
                ws.value(0, 1, "Вес (г)")
                rows.forEachIndexed { i, row ->
                    ws.value(i + 1, 0, "${row.brandName} ${row.flavorName}")
                    ws.value(i + 1, 1, row.weightGrams)
                }
            }
            out.toByteArray()
        }
    }
}
