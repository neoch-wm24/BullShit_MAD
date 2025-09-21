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

    // 当前选中的 Rack（入库时设置，出库后清除）
    private var currentRack: String = ""

    // 添加 Rack
    fun addRack(rack: RackInfo) {
        _rackList.add(rack)
    }

    // 获取所有 Rack 名称
    fun getRackNames(): List<String> {
        return _rackList.map { it.name }
    }

    // 通过名称获取 Rack
    fun getRackByName(name: String): RackInfo? {
        val target = name.trim().lowercase()
        return _rackList.find { it.name.trim().lowercase() == target }
    }

    // 通过ID获取 Rack
    fun getRackById(id: String): RackInfo? {
        return _rackList.find { it.id == id }
    }

    // 设置当前 Rack
    fun setCurrentRack(rack: String) {
        currentRack = rack
    }

    // 获取当前 Rack
    fun getCurrentRack(): String = currentRack

    // 清除当前 Rack
    fun clearCurrentRack() {
        currentRack = ""
    }
}