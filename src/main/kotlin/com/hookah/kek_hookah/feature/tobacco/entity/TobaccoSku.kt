package com.hookah.kek_hookah.feature.tobacco.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "tobacco_skus")
data class TobaccoSku(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flavor_id")
    val flavor: TobaccoFlavor,

    @Column(name = "weight_grams")
    val weightGrams: Int,

    @Column(name = "is_active")
    val isActive: Boolean = true
)