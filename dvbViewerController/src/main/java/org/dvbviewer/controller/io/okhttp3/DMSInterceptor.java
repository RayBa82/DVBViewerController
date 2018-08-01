package org.dvbviewer.controller.io.okhttp3;

import org.apache.commons.lang3.StringUtils;
import org.dvbviewer.controller.io.exception.AuthenticationException;
import org.dvbviewer.controller.io.exception.DefaultHttpException;
import org.dvbviewer.controller.io.exception.FileLockedException;
import org.dvbviewer.controller.io.exception.NoHostException;
import org.dvbviewer.controller.io.exception.UnsuccessfullHttpException;
import org.dvbviewer.controller.utils.URLUtil;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static org.dvbviewer.controller.utils.ServerConsts.REC_SERVICE_HOST;
import static org.dvbviewer.controller.utils.ServerConsts.REC_SERVICE_PASSWORD;
import static org.dvbviewer.controller.utils.ServerConsts.REC_SERVICE_USER_NAME;

public class DMSInterceptor implements Interceptor {


    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONNECTION = "Connection";

    @Override
    public Response intercept(Chain chain) throws IOException {
        if(StringUtils.isBlank(REC_SERVICE_HOST)) {
            throw new NoHostException();
        }
        Request request = chain.request();
        final HttpUrl requestUrl = request.url();
        final HttpUrl modifiedUrl = URLUtil.replaceUrl(requestUrl)
                .build();
        final String credentials = Credentials.basic(REC_SERVICE_USER_NAME, REC_SERVICE_PASSWORD);
        final Request modifiedRequest = request
                .newBuilder()
                .url(modifiedUrl)
                .addHeader(HEADER_AUTHORIZATION, credentials)
                .addHeader(HEADER_CONNECTION, "keep-alive")
                .build();

        final Response response = chain.proceed(modifiedRequest);
        if (!response.isSuccessful()) {
            final IOException e;
            switch (response.code()) {
                case 401:
                    e = new AuthenticationException();
                    break;
                case 423:
                    e =  new FileLockedException();
                    break;
                default:
                    e =  new UnsuccessfullHttpException(response.code());
                    break;
            }
            throw new DefaultHttpException(String.valueOf(modifiedUrl), e);
        }
        return response;
    }

}