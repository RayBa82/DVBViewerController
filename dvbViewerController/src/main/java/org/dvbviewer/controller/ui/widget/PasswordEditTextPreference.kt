package org.dvbviewer.controller.ui.widget

import android.content.Context
import android.text.InputType
import android.util.AttributeSet

import androidx.preference.EditTextPreference

class PasswordEditTextPreference : EditTextPreference, InputTypePref {

    override val inputType: Int
        get() = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int,
                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context) : super(context) {}

}