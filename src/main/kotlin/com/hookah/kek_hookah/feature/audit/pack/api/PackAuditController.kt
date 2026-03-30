package com.hookah.kek_hookah.feature.audit.pack.api

import com.hookah.kek_hookah.feature.audit.model.AuditEventType
import com.hookah.kek_hookah.feature.audit.pack.internal.usecase.ListPackAuditQuery
import com.hookah.kek_hookah.feature.audit.pack.model.PackAuditRecord
import com.hookah.kek_hookah.utils.crud.Slice
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/audit/pack")
class PackAuditController(private val query: ListPackAuditQuery) {

    @GetMapping
    suspend fun list(
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) after: UUID?,
        @RequestParam(required = false) userId: UUID?,
        @RequestParam(required = false) eventType: AuditEventType?,
        @RequestParam(required = false) entityId: UUID?,
    ): ResponseEntity<Slice<PackAuditRecord>> =
        query.execute(
            limit = limit.coerceIn(1, 100),
            afterId = after,
            userId = userId,
            eventType = eventType,
            entityId = entityId,
        ).let { ResponseEntity.ok(it) }
}
