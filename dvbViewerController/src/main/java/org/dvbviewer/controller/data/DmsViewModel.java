package org.dvbviewer.controller.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import org.dvbviewer.controller.R;
import org.dvbviewer.controller.data.api.io.exception.AuthenticationException;
import org.dvbviewer.controller.data.api.io.exception.DefaultHttpException;
import org.xml.sax.SAXException;

public abstract class DmsViewModel extends AndroidViewModel {


    public DmsViewModel(@NonNull Application application) {
        super(application);
    }

    protected String getErrorMessage(Throwable e) {
        final String message;
        if (e instanceof AuthenticationException) {
            message = getApplication().getString(R.string.error_invalid_credentials);
        } else if (e instanceof DefaultHttpException) {
            message = e.getMessage();
        } else if (e instanceof SAXException) {
            message = getApplication().getString(R.string.error_parsing_xml);
        } else {
            message = getApplication().getString(R.string.error_common)
                    + "\n\n"
                    + (e.getMessage() != null ? e.getMessage() : e.getClass().getName());
        }
        return message;
    }

}
