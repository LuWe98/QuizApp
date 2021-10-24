package com.example.quizapp.view.fragments.backgroptest

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RadioButton
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.backdrop.BackDropAnimListener
import com.example.quizapp.databinding.FragmentBackdropBinding
import com.example.quizapp.databinding.RviUserBinding
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.launch
import com.example.quizapp.extensions.log
import com.example.quizapp.extensions.onClick
import com.example.quizapp.utils.DiffUtilHelper
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class BackdropFragment : BindingFragment<FragmentBackdropBinding>() {

    private lateinit var sheet: BottomSheetBehavior<FrameLayout>

    private lateinit var rvAdapter: RvaTest

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBackDrop()
        initFrontLayer()
    }

    private fun initBackDrop(){
        binding.apply {
            backDropLayout.apply {
                setFrontLayerTopAnchor(backLayer.backLayerHeader)
//                setFrontLayerBotAnchor(binding.backLayer.chipGroup)
                frontLayerScrimView = frontLayer.scrimView


                addAnimProgressListener(object : BackDropAnimListener {
                    override fun onProgressChanged(progress: Float) {
//                    log("PROGRESS: $progress")
                    }
                })
            }

            backLayer.btnExpand.onClick {
                backDropLayout.toggle()
            }


            backLayer.btnAdd.onClick {
                binding.apply {
                    if(backLayer.root.childCount == 7){
                        backLayer.root.removeView(backLayer.chipGroup)
                        backLayer.root.removeView(backLayer.radioTitle)
                        backLayer.root.removeView(backLayer.radioGroup)
                    } else {
                        backLayer.root.addView(backLayer.chipGroup)
                        backLayer.root.addView(backLayer.radioTitle)
                        backLayer.root.addView(backLayer.radioGroup)
                    }

//                    val radioButton = RadioButton(requireContext()).apply {
//                        text = "TEST"
//                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//                    }
//                    backLayer.radioGroup.addView(radioButton)
//                    backDropLayout.adjustFrontLayer()
                }
            }
        }
    }

    private fun initFrontLayer(){
        rvAdapter = RvaTest().apply {

        }

        binding.frontLayer.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }

        rvAdapter.submitList(items)
    }

    companion object {
        val items = Array(20) { "Item $it"}.toList()
    }



    class RvaTest : BindingListAdapter<String, RviUserBinding>(DiffUtilHelper.createDiffUtil { t, t2 -> t == t2 }) {

        override fun initListeners(binding: RviUserBinding, vh: BindingListAdapterViewHolder) {

        }

        override fun bindViews(binding: RviUserBinding, item: String, position: Int) {
            binding.tvName.text = item
        }
    }
}