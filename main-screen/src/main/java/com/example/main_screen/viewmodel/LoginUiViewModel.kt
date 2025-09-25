package com.example.main_screen.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 保存 Login 页面纯 UI 状态（不涉及业务逻辑），防止因为重组 / 配置变化导致输入丢失。
 */
class LoginUiViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _employeeId = MutableStateFlow(savedStateHandle.get<String>(KEY_EMP_ID) ?: "")
    val employeeId: StateFlow<String> = _employeeId

    private val _password = MutableStateFlow(savedStateHandle.get<String>(KEY_PASSWORD) ?: "")
    val password: StateFlow<String> = _password

    private val _showPassword = MutableStateFlow(savedStateHandle.get<Boolean>(KEY_SHOW_PWD) ?: false)
    val showPassword: StateFlow<Boolean> = _showPassword

    private val _buttonPressed = MutableStateFlow(savedStateHandle.get<Boolean>(KEY_BTN_PRESSED) ?: false)
    val buttonPressed: StateFlow<Boolean> = _buttonPressed

    private val _selectedTab = MutableStateFlow(savedStateHandle.get<Int>(KEY_SELECTED_TAB) ?: 0)
    val selectedTab: StateFlow<Int> = _selectedTab

    fun setEmployeeId(value: String) {
        _employeeId.value = value
        savedStateHandle[KEY_EMP_ID] = value
    }

    fun setPassword(value: String) {
        _password.value = value
        savedStateHandle[KEY_PASSWORD] = value
    }

    fun toggleShowPassword() {
        val v = !_showPassword.value
        _showPassword.value = v
        savedStateHandle[KEY_SHOW_PWD] = v
    }

    fun setButtonPressed(value: Boolean) {
        _buttonPressed.value = value
        savedStateHandle[KEY_BTN_PRESSED] = value
    }

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
        savedStateHandle[KEY_SELECTED_TAB] = index
    }

    companion object {
        private const val KEY_EMP_ID = "login_employee_id"
        private const val KEY_PASSWORD = "login_password"
        private const val KEY_SHOW_PWD = "login_show_pwd"
        private const val KEY_BTN_PRESSED = "login_btn_pressed"
        private const val KEY_SELECTED_TAB = "login_selected_tab"
    }
}
