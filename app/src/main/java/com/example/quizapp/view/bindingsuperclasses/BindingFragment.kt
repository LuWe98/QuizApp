package com.example.quizapp.view.bindingsuperclasses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.quizapp.view.Navigator
import com.example.quizapp.utils.BindingUtils.getBinding
import javax.inject.Inject

abstract class BindingFragment<VB : ViewBinding> : Fragment() {

    @Inject lateinit var navigator: Navigator

    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        getBinding(this).also { _binding = it }.root

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}