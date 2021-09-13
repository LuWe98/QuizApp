package com.example.quizapp.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingActivity
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingDialogFragment
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingFragment
import java.lang.reflect.ParameterizedType

@Suppress("UNCHECKED_CAST")
object BindingUtils {
    private const val INFLATE_METHOD = "inflate"
    private const val FROM_BUNDLE_METHOD = "fromBundle"

    private fun findGenericTypeWith(classInstance: Any, genericClassToFind: Class<*>, relativePosition : Int): Class<*> {
        return try {
            (classInstance.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
                .map {
                    it as Class<*>
                }.filter {
                    genericClassToFind.isAssignableFrom(it)
                }[relativePosition]
        }  catch (e: Exception) {
            throw IllegalArgumentException("Could Not find generic Class '$genericClassToFind' of Instance '$classInstance'!")
        }
    }

    private fun <T : ViewBinding> getBindingWith (classInstance: Any, layoutInflater: LayoutInflater, relativePosition : Int) =
        findGenericTypeWith(classInstance, ViewBinding::class.java, relativePosition)
            .getMethod(INFLATE_METHOD, LayoutInflater::class.java)
            .invoke(null, layoutInflater) as T

    fun <T : ViewBinding> getViewHolderBindingWith(adapter: RecyclerView.Adapter<*>, parent: ViewGroup, relativePosition : Int = 0) =
        findGenericTypeWith(adapter, ViewBinding::class.java, relativePosition)
            .getMethod(INFLATE_METHOD, LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
            .invoke(null, LayoutInflater.from(parent.context), parent, false) as T


    fun <VM : ViewBinding> BindingActivity<VM>.getBinding(relativePosition : Int = 0) = getBindingWith(this, layoutInflater, relativePosition) as VM

    fun <VM : ViewBinding> BindingFragment<VM>.getBinding(relativePosition : Int = 0) = getBindingWith(this, layoutInflater, relativePosition) as VM

    fun <VM : ViewBinding> BindingDialogFragment<VM>.getBinding(relativePosition : Int = 0) = getBindingWith(this, layoutInflater, relativePosition) as VM

    fun <VM : ViewBinding> BindingBottomSheetDialogFragment<VM>.getBinding(relativePosition : Int = 0) = getBindingWith(this, layoutInflater, relativePosition) as VM
}