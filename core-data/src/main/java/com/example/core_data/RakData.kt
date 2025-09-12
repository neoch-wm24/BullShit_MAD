package com.example.core_data

import androidx.compose.runtime.mutableStateListOf

// 数据类来存储Rak信息
data class RakInfo(
    val id: String, // 添加唯一标识符
    val name: String,
    val layer: Int,
    val state: String = "Idle" // Default state is Idle
)

// 全局状态来存储Rak列表
object RakManager {
    private val _rakList = mutableStateListOf<RakInfo>()
    val rakList: List<RakInfo> get() = _rakList.toList()

    fun addRak(rak: RakInfo) {
        _rakList.add(rak)
    }

    fun getRakNames(): List<String> {
        return _rakList.map { it.name }
    }

    fun getRakByName(name: String): RakInfo? {
        val target = name.trim().lowercase()
        return _rakList.find { it.name.trim().lowercase() == target }
    }

    // 通过ID获取Rak信息
    fun getRakById(id: String): RakInfo? {
        return _rakList.find { it.id == id }
    }

    // 添加测试数据函数，用于测试滚动功能
    fun addTestData() {
        if (_rakList.isEmpty()) {
            repeat(20) { index ->
                _rakList.add(RakInfo(
                    id = "TestRakID${index + 1}", // 为测试数据生成唯一ID
                    name = "Test Rak ${index + 1}",
                    layer = (index % 5) + 1,
                    state = if (index % 2 == 0) "Idle" else "Non-Idle"
                ))
            }
        }
    }

    // 清空测试数据
    fun clearTestData() {
        _rakList.clear()
    }
}