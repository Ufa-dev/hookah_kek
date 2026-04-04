package com.hookah.kek_hookah.feature.reports.internal.usecase

import com.hookah.kek_hookah.feature.reports.internal.repository.ReportsRepository
import com.hookah.kek_hookah.feature.reports.model.StockReportRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Component
class StockReportUseCase(
    private val repository: ReportsRepository,
) {

    suspend fun generateXlsx(): ByteArray {
        val rows = repository.stockReport().toList()

        return withContext(Dispatchers.IO) {
            val out = ByteArrayOutputStream()
            ZipOutputStream(out).use { zip ->
                zip.entry("[Content_Types].xml", contentTypes())
                zip.entry("_rels/.rels", rootRels())
                zip.entry("xl/workbook.xml", workbook())
                zip.entry("xl/_rels/workbook.xml.rels", workbookRels())
                zip.entry("xl/worksheets/sheet1.xml", sheet(rows))
            }
            out.toByteArray()
        }
    }

    private fun ZipOutputStream.entry(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun contentTypes() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""

    private fun rootRels() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun workbook() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Остатки" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""

    private fun workbookRels() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""

    private fun sheet(rows: List<StockReportRow>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")
        sb.append("""<row r="1">""")
        sb.append(strCell("A1", "Название"))
        sb.append(strCell("B1", "Вес (г)"))
        sb.append("</row>")
        rows.forEachIndexed { i, row ->
            val r = i + 2
            sb.append("""<row r="$r">""")
            sb.append(strCell("A$r", "${row.brandName} ${row.flavorName}"))
            sb.append(numCell("B$r", row.weightGrams))
            sb.append("</row>")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun strCell(ref: String, value: String) =
        """<c r="$ref" t="inlineStr"><is><t>${xmlEscape(value)}</t></is></c>"""

    private fun numCell(ref: String, value: Long) =
        """<c r="$ref"><v>$value</v></c>"""

    private fun xmlEscape(s: String) = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}
