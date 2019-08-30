package org.dvbviewer.controller.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar

class ActionToolbar : Toolbar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun addView(child: View, params: ViewGroup.LayoutParams) {
        if (child is ActionMenuView) {
            params.width = LayoutParams.MATCH_PARENT
        }
        super.addView(child, params)
    }
}