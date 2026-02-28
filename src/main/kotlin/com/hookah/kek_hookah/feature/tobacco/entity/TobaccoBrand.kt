package com.hookah.kek_hookah.feature.tobacco.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "tobacco_brands")
data class TobaccoBrand(
    @Id
    val id: UUID = UUID.randomUUID(),

    val name: String,

    @Column(name = "is_active")
    val isActive: Boolean = true
)