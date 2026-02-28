package com.hookah.kek_hookah.infrastructure.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NullableNotBlankValidator::class])
annotation class NullableNotBlank(
    val message: String = "Value cannot be blank",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)

class NullableNotBlankValidator : ConstraintValidator<NullableNotBlank, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        return value == null || value.isNotBlank()
    }
}
