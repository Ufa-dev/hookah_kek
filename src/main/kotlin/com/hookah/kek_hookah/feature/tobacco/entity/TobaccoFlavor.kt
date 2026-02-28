package com.hookah.kek_hookah.feature.tobacco.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "tobacco_flavors")
data class TobaccoFlavor(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    val brand: TobaccoBrand,

    val name: String,
    val strength: String? = null,
    val category: String? = null,
    val description: String? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "is_featured")
    val isFeatured: Boolean = true
)