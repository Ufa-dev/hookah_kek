package com.hookah.kek_hookah.feature.tobacco.repository

import com.hookah.kek_hookah.feature.tobacco.entity.TobaccoBrand
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TobaccoBrandRepository : JpaRepository<TobaccoBrand, UUID>