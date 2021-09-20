package com.example.quizapp.extensions

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FlowLifecycleHelper<T>(
    lifecycleOwner: LifecycleOwner,
    private val flow: Flow<T>,
    private val collector: suspend (T) -> Unit,
    private val onState : Lifecycle.State,
) : DefaultLifecycleObserver {

    private var coroutineJob: Job? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        if(onState == Lifecycle.State.CREATED){
            collectFlow(owner)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if(onState == Lifecycle.State.STARTED){
            collectFlow(owner)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        if(onState == Lifecycle.State.RESUMED){
            collectFlow(owner)
        }
    }


    override fun onPause(owner: LifecycleOwner) {
        if(onState == Lifecycle.State.RESUMED){
            cancelFlow()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        if(onState == Lifecycle.State.STARTED){
            cancelFlow()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if(onState == Lifecycle.State.CREATED){
            cancelFlow()
        }
    }

    private fun collectFlow(owner: LifecycleOwner){
        coroutineJob = owner.lifecycleScope.launch {
            flow.collect {
                collector(it)
            }
        }
    }

    private fun cancelFlow(){
        coroutineJob?.cancel()
        coroutineJob = null
    }
}