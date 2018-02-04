package org.dvbviewer.controller.ui.widget;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;

public class PasswordEditTextPreference extends EditTextPreference implements InputTypePref {

    public PasswordEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                      int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PasswordEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PasswordEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PasswordEditTextPreference(Context context) {
        super(context);
    }

    @Override
    public int getInputType() {
        return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
    }

}