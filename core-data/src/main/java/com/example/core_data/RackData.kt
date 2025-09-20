package com.example.core_data

import androidx.compose.runtime.mutableStateListOf

// 数据类来存储Rack信息
data class RackInfo(
    val id: String, // 添加唯一标识符
    val name: String,
    val layer: Int,
    val state: String = "Idle" // Default state is Idle
)

// 全局状态来存储Rack列表
object RackManager {
    private val _rackList = mutableStateListOf<RackInfo>()
    val rackList: List<RackInfo> get() = _rackList.toList()

    fun addRack(rack: RackInfo) {
        _rackList.add(rack)
    }

    fun getRackNames(): List<String> {
        return _rackList.map { it.name }
    }

    fun getRackByName(name: String): RackInfo? {
        val target = name.trim().lowercase()
        return _rackList.find { it.name.trim().lowercase() == target }
    }

    // 通过ID获取Rack信息
    fun getRackById(id: String): RackInfo? {
        return _rackList.find { it.id == id }
    }

    // 添加测试数据函数，用于测试滚动功能
    fun addTestData() {
        if (_rackList.isEmpty()) {
            repeat(20) { index ->
                _rackList.add(RackInfo(
                    id = "TestRackID${index + 1}", // 为测试数据生成唯一ID
                    name = "Test Rack ${index + 1}",
                    layer = (index % 5) + 1,
                    state = if (index % 2 == 0) "Idle" else "Non-Idle"
                ))
            }
        }
    }

    // 清空测试数据
    fun clearTestData() {
        _rackList.clear()
    }
}