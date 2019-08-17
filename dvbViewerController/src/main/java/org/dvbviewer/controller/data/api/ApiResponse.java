package org.dvbviewer.controller.data.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.dvbviewer.controller.data.api.ApiStatus.ERROR;
import static org.dvbviewer.controller.data.api.ApiStatus.LOADING;
import static org.dvbviewer.controller.data.api.ApiStatus.NOT_SUPPORTED;
import static org.dvbviewer.controller.data.api.ApiStatus.SUCCESS;

public class ApiResponse<T>{

    @NonNull
    public final ApiStatus status;

    @Nullable
    public final String message;

    @Nullable
    public final Exception e;

    @Nullable
    public final T data;

    public ApiResponse(@NonNull ApiStatus status, @Nullable T data, @Nullable String message, @Nullable Exception e) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.e = e;
    }

    public static <T> ApiResponse<T> success(@Nullable T data) {
        return new ApiResponse<>(SUCCESS, data, null, null);
    }

    public static <T> ApiResponse<T> error(Exception e, @Nullable T data) {
        return new ApiResponse<>(ERROR, data, null, e);
    }

    public static <T> ApiResponse<T> notSupported(String msg) {
        return new ApiResponse<>(NOT_SUPPORTED, null, msg, null);
    }

    public static <T> ApiResponse<T> loading(@Nullable T data) {
        return new ApiResponse<>(LOADING, data, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApiResponse<?> resource = (ApiResponse<?>) o;

        if (status != resource.status) {
            return false;
        }
        if (message != null ? !message.equals(resource.message) : resource.message != null) {
            return false;
        }
        return data != null ? data.equals(resource.data) : resource.data == null;
    }

    @Override
    public int hashCode() {
        int result = status.hashCode();
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
