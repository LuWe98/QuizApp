package com.example.quizapp.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_NOT_VPN
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.*
import com.example.quizapp.extensions.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.SocketFactory

@Singleton
class ConnectivityHelper @Inject constructor(
    private val context: Context
) {

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val validNetworks: MutableSet<Network> = HashSet()
    private val connectivityMutableLiveData = ConnectivityLiveData(false)
    private val connectivityLiveData: LiveData<Boolean> get() = connectivityMutableLiveData.distinctUntilChanged()

    fun observeConnectivity(owner: LifecycleOwner, observer: Observer<Boolean>) {
        connectivityLiveData.observe(owner, observer)
    }

    fun removeObserver(observer: Observer<Boolean>) {
        connectivityLiveData.removeObserver(observer)
    }

    val isInternetAvailable get() = connectivityLiveData.value!!


    private fun postIfValidNetworkExists() {
        connectivityMutableLiveData.postValue(validNetworks.size != 0)
    }

    private inner class ConnectivityLiveData(initialState: Boolean? = null) : MutableLiveData<Boolean>(initialState) {
        override fun onActive() {
            super.onActive()
            NetworkRequest.Builder().addCapability(NET_CAPABILITY_INTERNET).build().let { request ->
                connectivityManager.registerNetworkCallback(request, networkCallback)
            }
        }

        override fun onInactive() {
            super.onInactive()
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            connectivityManager.getNetworkCapabilities(network)?.let {
                if (!it.hasCapability(NET_CAPABILITY_INTERNET)) {
                    return@let
                }
                validNetworks.add(network)
                postIfValidNetworkExists()

//                CoroutineScope(Dispatchers.IO).launch {
//                    if (doesNetworkHaveInternet(network.socketFactory)) {
//                        withContext(Dispatchers.Main) {
//                            validNetworks.add(network)
//                            postIfValidNetworkExists()
//                        }
//                    }
//                }
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            validNetworks.remove(network)
            postIfValidNetworkExists()
        }

        private fun doesNetworkHaveInternet(socketFactory: SocketFactory) = try {
            socketFactory.createSocket().apply {
                connect(InetSocketAddress("8.8.8.8", 53), 1500)
                close()
            }
            true
        } catch (e: IOException) {
            false
        }
    }

    companion object {
        fun isInternetAvailable(context: Context): Boolean {
            (context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager).let { manager ->
                val activeNetworks = manager.activeNetwork ?: return false
                val capabilities = manager.getNetworkCapabilities(activeNetworks) ?: return false
                return when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    else -> false
                }
            }
        }
    }
}