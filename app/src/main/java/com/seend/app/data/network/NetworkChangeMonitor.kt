package com.seend.app.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

enum class NetworkType { WIFI, MOBILE, UNKNOWN }
enum class ConnectionEvent { CONNECTED, CHANGED, DISCONNECTED, RECONNECTED }

data class NetworkChangeEvent(
    val type: NetworkType,
    val isAvailable: Boolean,
    val event: ConnectionEvent,
    val timestamp: Long = System.currentTimeMillis()
)

class NetworkChangeMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var currentNetwork: Network? = null
    private var isMonitoring = false
    private var lastType: NetworkType = NetworkType.UNKNOWN

    private val _networkChangeFlow = MutableSharedFlow<NetworkChangeEvent>(replay = 1)
    val networkChangeFlow: SharedFlow<NetworkChangeEvent> = _networkChangeFlow

    fun startMonitoring() {
        if (isMonitoring) return

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val newType = getNetworkType()
                val eventType = when {
                    !isAvailable() -> ConnectionEvent.CONNECTED
                    newType != lastType -> ConnectionEvent.CHANGED
                    else -> ConnectionEvent.RECONNECTED
                }
                lastType = newType
                currentNetwork = network

                scope.launch {
                    _networkChangeFlow.emit(
                        NetworkChangeEvent(type = newType, isAvailable = true, event = eventType)
                    )
                }
            }

            override fun onLost(network: Network) {
                if (currentNetwork == network) {
                    currentNetwork = null
                    lastType = NetworkType.UNKNOWN
                    scope.launch {
                        _networkChangeFlow.emit(
                            NetworkChangeEvent(type = NetworkType.UNKNOWN, isAvailable = false, event = ConnectionEvent.DISCONNECTED)
                        )
                    }
                }
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val newType = getNetworkType()
                if (newType != lastType) {
                    lastType = newType
                    scope.launch {
                        _networkChangeFlow.emit(
                            NetworkChangeEvent(type = newType, isAvailable = true, event = ConnectionEvent.CHANGED)
                        )
                    }
                }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback!!)
        isMonitoring = true
        lastType = getNetworkType()
    }

    fun stopMonitoring() {
        if (isMonitoring && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback!!)
            networkCallback = null
            isMonitoring = false
        }
    }

    fun isAvailable(): Boolean = currentNetwork != null

    private fun getNetworkType(): NetworkType {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?: return NetworkType.UNKNOWN
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.MOBILE
            else -> NetworkType.UNKNOWN
        }
    }
}
