package com.seend.app.data.network

import android.content.Context
import android.net.ConnectivityManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SpeedResult(val label: String) {
    S_2G_GSM("2G"),
    S_G_GPRS("GPRS"),
    S_E_EDGE("EDGE"),
    S_3G_UMTS("3G"),
    S_H_HSPA("HSPA"),
    S_HPLUS_HSPAPLUS("HSPA+"),
    S_4G_LTE("4G"),
    S_4G_LTEA("4G+"),
    S_5G("5G"),
    UNKNOWN("Desconocida")
}

data class ConnectionStatus(
    val download: SpeedResult = SpeedResult.UNKNOWN,
    val upload: SpeedResult = SpeedResult.UNKNOWN
)

class ConnectionManager(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _status = MutableStateFlow(ConnectionStatus())
    val status: StateFlow<ConnectionStatus> = _status.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        updateStatus()
        startAutoUpdate()
    }

    fun updateStatus() {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            _status.value = ConnectionStatus(
                download = mapSpeed(capabilities.linkDownstreamBandwidthKbps),
                upload = mapSpeed(capabilities.linkUpstreamBandwidthKbps)
            )
        } else {
            _status.value = ConnectionStatus()
        }
    }

    private fun mapSpeed(kbps: Int): SpeedResult = when {
        kbps <= 0 -> SpeedResult.UNKNOWN
        kbps <= 15 -> SpeedResult.S_2G_GSM
        kbps <= 27 -> SpeedResult.S_G_GPRS
        kbps <= 109 -> SpeedResult.S_E_EDGE
        kbps <= 128 -> SpeedResult.S_3G_UMTS
        kbps <= 3685 -> SpeedResult.S_H_HSPA
        kbps <= 23585 -> SpeedResult.S_HPLUS_HSPAPLUS
        kbps <= 51201 -> SpeedResult.S_4G_LTE
        kbps <= 524289 -> SpeedResult.S_4G_LTEA
        else -> SpeedResult.S_5G
    }

    private fun startAutoUpdate() {
        scope.launch {
            while (isActive) {
                delay(10000)
                updateStatus()
            }
        }
    }

    fun getCurrentSpeedLabel(): String = _status.value.download.label
}
