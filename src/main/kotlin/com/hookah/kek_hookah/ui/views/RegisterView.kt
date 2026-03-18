package com.hookah.kek_hookah.ui.views

import com.hookah.kek_hookah.feature.auth.AuthService
import com.hookah.kek_hookah.feature.auth.api.dto.RegisterRequest
import com.hookah.kek_hookah.ui.context.AuthContext
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.dom.Style
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLink
import kotlinx.coroutines.runBlocking

@Route("register")
@PageTitle("Register")
class RegisterView(
    private val authService: AuthService
) : VerticalLayout() {

    private val emailField = EmailField("Email")
    private val nameField = TextField("Name")
    private val passwordField = PasswordField("Password")
    private val registerButton = Button("Register")
    private val loginLink = RouterLink("Already have an account? Login", LoginView::class.java)

    init {
        addClassName("register-view")
        setSizeFull()
        alignItems = FlexComponent.Alignment.CENTER
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER)

        emailField.isRequired = true
        nameField.isRequired = true
        passwordField.isRequired = true

        registerButton.addClickListener {
            runBlocking {
                val request = RegisterRequest(emailField.value, nameField.value, passwordField.value)
                try {
                    val response = authService.register(request)
                    AuthContext.get().updateFromAuthResponse(response)
                    UI.getCurrent().navigate(LoginView::class.java)
                } catch (e: Exception) {
                    Notification.show("Registration failed: ${e.message}")
                }
            }
        }

        add(
            H1("Register"),
            emailField,
            nameField,
            passwordField,
            registerButton,
            loginLink
        )
    }
}