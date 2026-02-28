package com.hookah.kek_hookah.utils.uuid

import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator
import java.util.UUID

private val generator: TimeBasedEpochRandomGenerator = Generators.timeBasedEpochRandomGenerator()

fun uuidV7(): UUID = generator.generate()
