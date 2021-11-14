package com.example.quizapp.view.customimplementations.lazytablayout

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.extensions.log

//TODO -> Scroll geht soweit | einbauen, dass es custom view ist wie bei TabLayout, wo es getriggert wird, wenn ein tab erstellt wird
//TODO -> Adapter genauer anschauen, internen adapter vermutlich?
//TODO -> Adapter einfach so lassen, dass er extern ist

class LazyTabLayoutManager(private val lazyTabLayout: LazyTabLayout)  : LinearLayoutManager(lazyTabLayout.context, HORIZONTAL, false)  {

    override fun generateDefaultLayoutParams() = RecyclerView.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    override fun canScrollVertically() = false
    override fun canScrollHorizontally() = true

}