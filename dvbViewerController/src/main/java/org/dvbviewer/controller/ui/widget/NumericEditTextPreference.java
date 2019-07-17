package org.dvbviewer.controller.ui.widget;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

public class NumericEditTextPreference extends EditTextPreference implements  InputTypePref{

    public NumericEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                     int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public NumericEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NumericEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumericEditTextPreference(Context context) {
        super(context);
    }

    @Override
    public int getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }
}