package com.example.quizapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.*
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ConnectivityUtil {

    fun createConnectivityHelper(fragment: Fragment) = ConnectivityWrapper(fragment.requireContext(), fragment.lifecycle)

    fun createConnectivityHelper(activity: AppCompatActivity) = ConnectivityWrapper(activity, activity.lifecycle)

    fun createConnectivityHelper(context: Context, lifecycle: Lifecycle) = ConnectivityWrapper(context, lifecycle)

    class ConnectivityWrapper constructor(context: Context, lifecycle: Lifecycle) {

        private val connectivityManager by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
        private val validNetworks: MutableSet<Network> by lazy { HashSet() }
        private val connectivityMutableStateFlow by lazy { MutableStateFlow<Boolean?>(null) }

        val connectivityStateFlow: StateFlow<Boolean?> get() = connectivityMutableStateFlow.asStateFlow()

        init {
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    super.onResume(owner)
                    NetworkRequest.Builder().addCapability(NET_CAPABILITY_INTERNET).build().let { request ->
                        connectivityManager.registerNetworkCallback(request, networkCallback)
                    }
                }

                override fun onPause(owner: LifecycleOwner) {
                    super.onPause(owner)
                    connectivityManager.unregisterNetworkCallback(networkCallback)
                }
            })
        }

        private fun updateStateFlowValue() {
            connectivityMutableStateFlow.value = validNetworks.size != 0
        }

        private val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                connectivityManager.getNetworkCapabilities(network)?.let {
                    if (!it.hasCapability(NET_CAPABILITY_INTERNET)) {
                        return@let
                    }
                    validNetworks.add(network)
                    updateStateFlowValue()
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                validNetworks.remove(network)
                updateStateFlowValue()
            }
        }
    }
}