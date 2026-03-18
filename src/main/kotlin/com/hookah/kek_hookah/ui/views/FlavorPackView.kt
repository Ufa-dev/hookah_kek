package com.hookah.kek_hookah.ui.views

import com.hookah.kek_hookah.feature.tobacco.brand.BrandService
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.FlavorService
import com.hookah.kek_hookah.feature.tobacco.pack.FlavorPackService
import com.hookah.kek_hookah.feature.tobacco.brand.model.TabacoBrand
import com.hookah.kek_hookah.feature.tobacco.flavor.model.TabacoFlavor
import com.hookah.kek_hookah.feature.tobacco.pack.model.PackId
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPackForCreate
import com.hookah.kek_hookah.ui.context.AuthContext
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import kotlinx.coroutines.runBlocking

@Route("admin/flavor-pack")
@PageTitle("Add Flavor Pack")
class FlavorPackView(
    private val brandService: BrandService,
    private val flavorService: FlavorService,
    private val flavorPackService: FlavorPackService
) : VerticalLayout(), BeforeEnterObserver {

    private val scanButton = Button("Scan Tag")
    private val tagIdField = TextField("Tag ID").apply {
        isVisible = false
        isReadOnly = true
    }
    private val brandCombo = ComboBox<TabacoBrand>("Brand").apply {
        isVisible = false
        setItemLabelGenerator { it.name }
    }
    private val flavorCombo = ComboBox<TabacoFlavor>("Flavor").apply {
        isVisible = false
        setItemLabelGenerator { it.name }
    }
    private val totalWeightField = IntegerField("Total Weight (g)").apply {
        isVisible = false
        min = 1
        value = 50 // значение по умолчанию
    }
    private val currentWeightField = IntegerField("Current Weight (g)").apply {
        isVisible = false
        min = 0
        value = 50 // по умолчанию равно общему весу
    }
    private val saveButton = Button("Save").apply { isVisible = false }

    private var scannedTagId: String? = null

    init {
        addClassName("flavor-pack-view")
        setSizeFull()
        alignItems = FlexComponent.Alignment.CENTER
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER)

        val title = H2("Add New Flavor Pack")
        add(title, scanButton, tagIdField, brandCombo, flavorCombo, totalWeightField, currentWeightField, saveButton)

        scanButton.addClickListener {
            simulateScan()
        }

        brandCombo.addValueChangeListener { event ->
            val brand = event.value
            if (brand != null) {
                loadFlavorsForBrand(brand.id)
            } else {
                flavorCombo.clear()
                flavorCombo.isVisible = false
                hideWeightFields()
            }
        }

        saveButton.addClickListener {
            saveFlavorPack()
        }
    }

    private fun simulateScan() {
        // Заглушка: возвращаем фиктивную метку
        val mockTagId = "TAG-${System.currentTimeMillis()}"
        scannedTagId = mockTagId
        tagIdField.value = mockTagId
        tagIdField.isVisible = true

        // Загружаем бренды
        runBlocking {
            val brands = brandService.findAll()
            brandCombo.setItems(brands)
            brandCombo.isVisible = true
        }
    }

    private fun loadFlavorsForBrand(brandId: BrandId) {
        runBlocking {
            val flavors = flavorService.findByBrandId(brandId)
            flavorCombo.setItems(flavors)
            flavorCombo.isVisible = true
            // После загрузки вкусов показываем поля веса
            totalWeightField.isVisible = true
            currentWeightField.isVisible = true
            saveButton.isVisible = true
        }
    }

    private fun hideWeightFields() {
        totalWeightField.isVisible = false
        currentWeightField.isVisible = false
        saveButton.isVisible = false
    }
    private fun saveFlavorPack() {
        val tagId = scannedTagId
        val flavor = flavorCombo.value
        val totalWeight = totalWeightField.value
        val currentWeight = currentWeightField.value

        if (tagId == null || flavor == null || totalWeight == null || currentWeight == null) {
            Notification.show("Please fill all fields")
            return
        }

        val userId = AuthContext.get().userId
        if (userId == null) {
            Notification.show("User not authenticated")
            return
        }

        val command = FlavorPackForCreate(
            id = PackId(tagId),
            flavorId = flavor.id,
            currentWeightGrams = currentWeight.toLong(),   // преобразуем Int в Long
            totalWeightGrams = totalWeight.toLong(),       // преобразуем Int в Long
            userId = userId
        )

        runBlocking {
            try {
                flavorPackService.create(command)
                Notification.show("Flavor pack saved successfully")
                clearForm()
            } catch (e: Exception) {
                Notification.show("Error: ${e.message}")
            }
        }
    }

    private fun clearForm() {
        tagIdField.clear()
        brandCombo.clear()
        flavorCombo.clear()
        totalWeightField.clear()
        currentWeightField.clear()
        scannedTagId = null
        tagIdField.isVisible = false
        brandCombo.isVisible = false
        flavorCombo.isVisible = false
        totalWeightField.isVisible = false
        currentWeightField.isVisible = false
        saveButton.isVisible = false
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        if (!AuthContext.get().isAuthenticated()) {
            event.forwardTo(LoginView::class.java)
        }
    }
}