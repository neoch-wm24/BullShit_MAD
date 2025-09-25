package com.example.delivery_and_transportation_management.ui.screen

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.delivery_and_transportation_management.R
import com.example.delivery_and_transportation_management.data.DeliveryViewModel
import com.example.delivery_and_transportation_management.data.Stop
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class DriverMapActivity : ComponentActivity() {

    private lateinit var mapView: MapView
    private lateinit var deliveryViewModel: DeliveryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 OSMDroid 配置
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = packageName

        deliveryViewModel = ViewModelProvider(this)[DeliveryViewModel::class.java]

        setupMapView()
        setupRouteFromIntent()

        setContentView(mapView)
    }

    private fun setupMapView() {
        mapView = MapView(this)
        mapView.setTileSource(TileSourceFactory.MAPNIK) // 免费的 OpenStreetMap 瓦片
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(12.0)

        // 默认中心点：吉隆坡
        val defaultCenter = GeoPoint(3.1390, 101.6869)
        mapView.controller.setCenter(defaultCenter)
    }

    private fun setupRouteFromIntent() {
        val employeeId = intent.getStringExtra("employeeId") ?: ""
        val selectedDate = intent.getStringExtra("selectedDate") ?: ""

        if (employeeId.isNotEmpty()) {
            // 使用 Flow 而不是 LiveData
            lifecycleScope.launch {
                deliveryViewModel.deliveries.collect { allDeliveries ->
                    val driverDeliveries = allDeliveries.filter { delivery ->
                        delivery.employeeID.equals(employeeId, true) &&
                        (delivery.date == selectedDate || selectedDate.isEmpty())
                    }

                    if (driverDeliveries.isNotEmpty()) {
                        displayRoute(driverDeliveries)
                    }
                }
            }
        } else {
            // 显示单个传递的 stop
            val lat = intent.getDoubleExtra("lat", 3.1390)
            val lng = intent.getDoubleExtra("lng", 101.6869)
            val name = intent.getStringExtra("name") ?: "Delivery Stop"
            val address = intent.getStringExtra("address") ?: ""

            val stop = Stop(name, address, GeoPoint(lat, lng))
            displaySingleStop(stop)
        }
    }

    private fun displayRoute(deliveries: List<com.example.delivery_and_transportation_management.data.Delivery>) {
        mapView.overlays.clear()

        // TARUMT 起始点
        val tarumtStart = GeoPoint(3.2155, 101.7280)
        val startAddress = "Ground Floor, Bangunan Tan Sri Khaw Kai Boh (Block A), Jalan Genting Kelang, Setapak, 53300 Kuala Lumpur"

        val allStops = mutableListOf<Stop>()
        allStops.add(Stop("TARUMT - Block A Ground Floor", startAddress, tarumtStart))

        // 添加所有配送点
        deliveries.forEach { delivery ->
            allStops.addAll(delivery.stops)
        }

        if (allStops.size > 1) {
            // 绘制路线
            drawRoutePolyline(allStops)

            // 添加标记
            addMarkersForStops(allStops)

            // 调整地图视图以显示所有点
            zoomToShowAllStops(allStops)
        } else if (allStops.size == 1) {
            // 只有起始点
            addMarkersForStops(allStops)
            mapView.controller.setCenter(allStops[0].location)
            mapView.controller.setZoom(15.0)
        }
    }

    private fun displaySingleStop(stop: Stop) {
        mapView.overlays.clear()

        val marker = Marker(mapView)
        marker.position = stop.location
        marker.title = stop.name
        marker.snippet = stop.address

        // 自定义标记图标 (可选)
        try {
            val drawable: Drawable? = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_mylocation)
            marker.icon = drawable
        } catch (e: Exception) {
            // 使用默认图标
        }

        mapView.overlays.add(marker)
        mapView.controller.setCenter(stop.location)
        mapView.controller.setZoom(15.0)

        mapView.invalidate()
    }

    private fun drawRoutePolyline(stops: List<Stop>) {
        if (stops.size < 2) return

        // 主路线 (红色)
        val mainPolyline = Polyline()
        mainPolyline.setPoints(stops.map { it.location })
        mainPolyline.color = android.graphics.Color.RED
        mainPolyline.width = 8.0f
        mapView.overlays.add(mainPolyline)

        // 辅助路线 (蓝色，更细)
        val secondaryPolyline = Polyline()
        secondaryPolyline.setPoints(stops.map { it.location })
        secondaryPolyline.color = android.graphics.Color.BLUE
        secondaryPolyline.width = 4.0f
        mapView.overlays.add(secondaryPolyline)
    }

    private fun addMarkersForStops(stops: List<Stop>) {
        stops.forEachIndexed { index, stop ->
            val marker = Marker(mapView)
            marker.position = stop.location

            if (index == 0) {
                marker.title = "🏢 START: ${stop.name}"
                marker.snippet = stop.address
                // 起始点用不同颜色/图标
                try {
                    val drawable: Drawable? = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_compass)
                    marker.icon = drawable
                } catch (e: Exception) {
                    // 使用默认图标
                }
            } else {
                marker.title = "📦 DELIVERY $index: ${stop.name}"
                marker.snippet = "${stop.address}\n\nTap to navigate"

                // 点击标记时的行为
                marker.setOnMarkerClickListener { marker, mapView ->
                    // 可以在这里添加导航逻辑，比如打开 Google Maps 导航
                    openExternalNavigation(stop)
                    true
                }
            }

            mapView.overlays.add(marker)
        }
    }

    private fun openExternalNavigation(stop: Stop) {
        // 打开外部地图应用进行导航
        val gmmIntentUri = android.net.Uri.parse(
            "geo:${stop.location.latitude},${stop.location.longitude}?q=${stop.location.latitude},${stop.location.longitude}(${stop.name})"
        )
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps") // 优先使用 Google Maps

        try {
            startActivity(mapIntent)
        } catch (e: Exception) {
            // 如果没有 Google Maps，使用通用 geo intent
            val genericIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            if (genericIntent.resolveActivity(packageManager) != null) {
                startActivity(genericIntent)
            }
        }
    }

    private fun zoomToShowAllStops(stops: List<Stop>) {
        if (stops.isEmpty()) return

        val latitudes = stops.map { it.location.latitude }
        val longitudes = stops.map { it.location.longitude }

        val minLat = latitudes.minOrNull() ?: return
        val maxLat = latitudes.maxOrNull() ?: return
        val minLng = longitudes.minOrNull() ?: return
        val maxLng = longitudes.maxOrNull() ?: return

        val centerLat = (minLat + maxLat) / 2
        val centerLng = (minLng + maxLng) / 2

        mapView.controller.setCenter(GeoPoint(centerLat, centerLng))

        // 计算合适的缩放级别
        val latSpan = maxLat - minLat
        val lngSpan = maxLng - minLng
        val maxSpan = maxOf(latSpan, lngSpan)

        val zoom = when {
            maxSpan > 0.5 -> 10.0
            maxSpan > 0.1 -> 12.0
            maxSpan > 0.05 -> 13.0
            else -> 14.0
        }

        mapView.controller.setZoom(zoom)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDetach()
    }
}
