package org.dvbviewer.controller.utils;

import android.os.Parcel;
import android.os.Parcelable;

public enum StreamType implements Parcelable {

        DIRECT, TRANSCODED;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeInt(ordinal());
        }

        public static final Creator<StreamType> CREATOR = new Creator<StreamType>() {
            @Override
            public StreamType createFromParcel(final Parcel source) {
                return StreamType.values()[source.readInt()];
            }

            @Override
            public StreamType[] newArray(final int size) {
                return new StreamType[size];
            }
        };
    }