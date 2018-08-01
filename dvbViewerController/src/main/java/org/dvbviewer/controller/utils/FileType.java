package org.dvbviewer.controller.utils;

import android.os.Parcel;
import android.os.Parcelable;

public enum FileType implements Parcelable {

    CHANNEL("chid", "channelstream"), RECORDING("recid", "recordings"), VIDEO("objid", "video");

    public String transcodedParam;
    public String directPath;

    FileType(String transcodedParam, String directPath) {
        this.transcodedParam = transcodedParam;
        this.directPath = directPath;
    }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeInt(ordinal());
        }

        public static final Creator<FileType> CREATOR = new Creator<FileType>() {
            @Override
            public FileType createFromParcel(final Parcel source) {
                return FileType.values()[source.readInt()];
            }

            @Override
            public FileType[] newArray(final int size) {
                return new FileType[size];
            }
        };
    }