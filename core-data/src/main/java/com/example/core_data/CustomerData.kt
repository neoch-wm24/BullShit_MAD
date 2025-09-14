package com.example.core_data

import androidx.compose.runtime.mutableStateListOf

// 数据类来存储 Customer 信息
data class CustomerInfo(
    val id: String,         // 唯一标识符
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val postcode: String,
    val city: String,
    val state: String
)

// 全局状态来存储 Customer 列表
object CustomerManager {
    private val _customerList = mutableStateListOf<CustomerInfo>()
    val customerList: List<CustomerInfo> get() = _customerList.toList()

    fun addCustomer(customer: CustomerInfo) {
        _customerList.add(customer)
    }

    fun getCustomerNames(): List<String> {
        return _customerList.map { it.name }
    }

    fun getCustomerByName(name: String): CustomerInfo? {
        val target = name.trim().lowercase()
        return _customerList.find { it.name.trim().lowercase() == target }
    }

    fun getCustomerById(id: String): CustomerInfo? {
        return _customerList.find { it.id == id }
    }

    // 添加测试数据函数
    fun addTestData() {
        if (_customerList.isEmpty()) {
            repeat(10) { index ->
                _customerList.add(
                    CustomerInfo(
                        id = "TestCustomerID${index + 1}",
                        name = "Customer ${index + 1}",
                        phone = "012-345678${index}",
                        email = "customer${index + 1}@example.com",
                        address = "Street ${index + 1}, Example Area",
                        postcode = "1234${index}",
                        city = "City ${index + 1}",
                        state = "State ${index + 1}"
                    )
                )
            }
        }
    }

    fun clearTestData() {
        _customerList.clear()
    }
}
