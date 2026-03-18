package com.hookah.kek_hookah.ui.views

import com.hookah.kek_hookah.feature.auth.AuthService
import com.hookah.kek_hookah.feature.auth.api.dto.CreditsToLogin
import com.hookah.kek_hookah.feature.tobacco.pack.model.FlavorPack
import com.hookah.kek_hookah.ui.context.AuthContext
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.dom.Style
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLink
import kotlinx.coroutines.runBlocking
import kotlin.jvm.java

@Route("login")
@PageTitle("Login")
class LoginView(
    private val authService: AuthService
) : VerticalLayout() {

    private val emailField = EmailField("Email")
    private val passwordField = PasswordField("Password")
    private val loginButton = Button("Login")
    private val registerLink = RouterLink("Don't have an account? Register", RegisterView::class.java)

    init {
        addClassName("login-view")
        setSizeFull()
        alignItems = FlexComponent.Alignment.CENTER
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER)

        emailField.isRequired = true
        passwordField.isRequired = true

        loginButton.addClickListener {
            runBlocking {
                val request = CreditsToLogin(emailField.value, passwordField.value)
                try {
                    val response = authService.login(request)
                    AuthContext.get().updateFromAuthResponse(response)
                    // Перенаправляем на админку (или главную страницу)
                    UI.getCurrent().navigate(FlavorPackView::class.java)
                } catch (e: Exception) {
                    Notification.show("Login failed: ${e.message}")
                }
            }
        }

        add(
            H1("Login"),
            emailField,
            passwordField,
            loginButton,
            registerLink
        )
    }
}