package com.hookah.kek_hookah.feature.reports.internal.repository

import com.hookah.kek_hookah.feature.reports.model.StockReportRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

@Component
class ReportsRepository(
    private val db: DatabaseClient,
) {

    fun stockReport(): Flow<StockReportRow> =
        db.sql(STOCK_QUERY)
            .map { row, _ ->
                StockReportRow(
                    brandName = row.get("brand_name", String::class.java)!!,
                    flavorName = row.get("flavor_name", String::class.java)!!,
                    weightGrams = (row.get("weight_grams", Number::class.java) ?: 0).toLong(),
                )
            }
            .all()
            .asFlow()

    companion object {
        private const val STOCK_QUERY = """
            WITH market_totals AS (
                SELECT ma.tabacoo_flavor_id AS flavor_id,
                       b.name              AS brand_name,
                       f.name              AS flavor_name,
                       SUM(ma.weight_grams::BIGINT * ma.count) AS market_weight_grams
                FROM market_arc ma
                JOIN tabacoo_brand  b ON b.id = ma.brand_id
                JOIN tabacoo_flavor f ON f.id = ma.tabacoo_flavor_id
                WHERE ma.count > 0
                GROUP BY ma.tabacoo_flavor_id, b.name, f.name
            ),
            pack_totals AS (
                SELECT fp.flavor_id,
                       SUM(fp.current_weight_grams)::BIGINT AS pack_weight_grams
                FROM flavor_pack fp
                WHERE fp.flavor_id IS NOT NULL
                GROUP BY fp.flavor_id
            )
            SELECT COALESCE(mt.brand_name, b2.name)  AS brand_name,
                   COALESCE(mt.flavor_name, f2.name) AS flavor_name,
                   COALESCE(mt.market_weight_grams, 0)
                     + COALESCE(pt.pack_weight_grams, 0) AS weight_grams
            FROM   market_totals mt
            FULL OUTER JOIN pack_totals pt ON pt.flavor_id = mt.flavor_id
            LEFT  JOIN tabacoo_flavor f2   ON f2.id = pt.flavor_id
            LEFT  JOIN tabacoo_brand  b2   ON b2.id = f2.brand_id
            ORDER BY brand_name, flavor_name
        """
    }
}
