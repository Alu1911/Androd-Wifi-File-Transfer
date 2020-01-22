/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.Devlex.iWifiFileTransfer.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ProtocolException;

import com.Devlex.iWifiFileTransfer.R;
import com.Devlex.iWifiFileTransfer.libcore.util.Objects;
import com.Devlex.iWifiFileTransfer.misc.IconUtils;
import com.Devlex.iWifiFileTransfer.model.DocumentsContract.Root;
import com.Devlex.iWifiFileTransfer.provider.DownloadStorageProvider;
import com.Devlex.iWifiFileTransfer.provider.ExternalStorageProvider;
import com.Devlex.iWifiFileTransfer.provider.MediaDocumentsProvider;
import com.Devlex.iWifiFileTransfer.provider.NonMediaDocumentsProvider;
import com.Devlex.iWifiFileTransfer.provider.RecentsProvider;
import com.Devlex.iWifiFileTransfer.provider.RootedStorageProvider;
import com.Devlex.iWifiFileTransfer.provider.UsbStorageProvider;

import static com.Devlex.iWifiFileTransfer.model.DocumentInfo.getCursorInt;
import static com.Devlex.iWifiFileTransfer.model.DocumentInfo.getCursorLong;
import static com.Devlex.iWifiFileTransfer.model.DocumentInfo.getCursorString;

/**
 * Representation of a {@link Root}.
 */
@SuppressLint("DefaultLocale")
public class RootInfo implements Durable, Parcelable {
    @SuppressWarnings("unused")
	private static final int VERSION_INIT = 1;
    private static final int VERSION_DROP_TYPE = 2;

    public String authority;
    public String rootId;
    public int flags;
    public int icon;
    public String title;
    public String summary;
    public String documentId;
    public long availableBytes;
    public long totalBytes;
    public String mimeTypes;
    public String path;
    public File visiblePath;

    /** Derived fields that aren't persisted */
    public String derivedPackageName;
    public String[] derivedMimeTypes;
    public int derivedIcon;
    public int derivedColor;
    public String derivedTag;

    public RootInfo() {
        reset();
    }

    @Override
    public void reset() {
        authority = null;
        rootId = null;
        flags = 0;
        icon = 0;
        title = null;
        summary = null;
        documentId = null;
        availableBytes = -1;
        totalBytes = -1;
        mimeTypes = null;
        path = null;

        derivedPackageName = null;
        derivedMimeTypes = null;
        derivedIcon = 0;
        derivedColor = 0;
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        final int version = in.readInt();
        switch (version) {
            case VERSION_DROP_TYPE:
                authority = DurableUtils.readNullableString(in);
                rootId = DurableUtils.readNullableString(in);
                flags = in.readInt();
                icon = in.readInt();
                title = DurableUtils.readNullableString(in);
                summary = DurableUtils.readNullableString(in);
                documentId = DurableUtils.readNullableString(in);
                availableBytes = in.readLong();
                totalBytes = in.readLong();
                mimeTypes = DurableUtils.readNullableString(in);
                path = DurableUtils.readNullableString(in);
                deriveFields();
                break;
            default:
                throw new ProtocolException("Unknown version " + version);
        }
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(VERSION_DROP_TYPE);
        DurableUtils.writeNullableString(out, authority);
        DurableUtils.writeNullableString(out, rootId);
        out.writeInt(flags);
        out.writeInt(icon);
        DurableUtils.writeNullableString(out, title);
        DurableUtils.writeNullableString(out, summary);
        DurableUtils.writeNullableString(out, documentId);
        out.writeLong(availableBytes);
        out.writeLong(totalBytes);
        DurableUtils.writeNullableString(out, mimeTypes);
        DurableUtils.writeNullableString(out, path);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        DurableUtils.writeToParcel(dest, this);
    }

    public static final Creator<RootInfo> CREATOR = new Creator<RootInfo>() {
        @Override
        public RootInfo createFromParcel(Parcel in) {
            final RootInfo root = new RootInfo();
            DurableUtils.readFromParcel(in, root);
            return root;
        }

        @Override
        public RootInfo[] newArray(int size) {
            return new RootInfo[size];
        }
    };

    public static RootInfo fromRootsCursor(String authority, Cursor cursor) {
        final RootInfo root = new RootInfo();
        root.authority = authority;
        root.rootId = getCursorString(cursor, Root.COLUMN_ROOT_ID);
        root.flags = getCursorInt(cursor, Root.COLUMN_FLAGS);
        root.icon = getCursorInt(cursor, Root.COLUMN_ICON);
        root.title = getCursorString(cursor, Root.COLUMN_TITLE);
        root.summary = getCursorString(cursor, Root.COLUMN_SUMMARY);
        root.documentId = getCursorString(cursor, Root.COLUMN_DOCUMENT_ID);
        root.availableBytes = getCursorLong(cursor, Root.COLUMN_AVAILABLE_BYTES);
        root.totalBytes = getCursorLong(cursor, Root.COLUMN_CAPACITY_BYTES);
        root.mimeTypes = getCursorString(cursor, Root.COLUMN_MIME_TYPES);
        root.path = getCursorString(cursor, Root.COLUMN_PATH);
        root.deriveFields();
        return root;
    }

    public void deriveFields() {
        derivedMimeTypes = (mimeTypes != null) ? mimeTypes.split("\n") : null;
        derivedTag = title;

        // TODO: remove these special case icons


    }

/*    public boolean isHome() {
        // Note that "home" is the expected root id for the auto-created
        // user home directory on external storage. The "home" value should
        // match ExternalStorageProvider.ROOT_ID_HOME.
        return isExternalStorage() && "home".equals(rootId);
    }*/

    public boolean isHome() {
        return authority == null && "home".equals(rootId);
    }

    public boolean isConnections() {
        return authority == null && "connections".equals(rootId);
    }

    public boolean isRecents() {
        return RecentsProvider.AUTHORITY.equals(authority) && "recents".equals(rootId);
    }

    public boolean isStorage() {
        return isInternalStorage() || isExternalStorage() || isSecondaryStorage();
    }

    public boolean isRootedStorage() {
        return RootedStorageProvider.AUTHORITY.equals(authority);
    }

    public boolean isExternalStorage() {
        return ExternalStorageProvider.AUTHORITY.equals(authority)
                && ExternalStorageProvider.ROOT_ID_PRIMARY_EMULATED.equals(rootId);
    }

    public boolean isInternalStorage() {
        return ExternalStorageProvider.AUTHORITY.equals(authority)
                && title.toLowerCase().contains("internal");
    }

    public boolean isPhoneStorage() {
        return ExternalStorageProvider.AUTHORITY.equals(authority)
                && ExternalStorageProvider.ROOT_ID_PHONE.equals(rootId);
    }
    
    public boolean isSecondaryStorage() {
        return ExternalStorageProvider.AUTHORITY.equals(authority)
        		&& rootId.startsWith(ExternalStorageProvider.ROOT_ID_SECONDARY);
    }

    public boolean isSecondaryStorageSD() {
        return contains(path, "sd", "card", "emmc") || contains(title, "sd", "card", "emmc");
    }
    
    public boolean isSecondaryStorageUSB() {
        return contains(path, "usb") || contains(title, "usb");
    }
    
    public boolean isSecondaryStorageHDD() {
        return contains(path, "hdd") || contains(title, "hdd");
    }

    public boolean isDownloadsFolder() {
        return ExternalStorageProvider.AUTHORITY.equals(authority)
                && ExternalStorageProvider.ROOT_ID_DOWNLOAD.equals(rootId);
    }

    public boolean isAppBackupFolder() {
        return ExternalStorageProvider.AUTHORITY.equals(authority)
                && ExternalStorageProvider.ROOT_ID_APP_BACKUP.equals(rootId);
    }

    public boolean isBluetoothFolder() {
        return ExternalStorageProvider.AUTHORITY.equals(authority)
                && ExternalStorageProvider.ROOT_ID_BLUETOOTH.equals(rootId);
    }

    public boolean isBookmarkFolder() {
        return ExternalStorageProvider.AUTHORITY.equals(authority)
                && rootId.startsWith(ExternalStorageProvider.ROOT_ID_BOOKMARK);
    }

    public boolean isHiddenFolder() {
        return ExternalStorageProvider.AUTHORITY.equals(authority)
                && ExternalStorageProvider.ROOT_ID_HIDDEN.equals(rootId);
    }
    
    public boolean isDownloads() {
        return DownloadStorageProvider.AUTHORITY.equals(authority);
    }

    public boolean isImages() {
        return MediaDocumentsProvider.AUTHORITY.equals(authority)
                && MediaDocumentsProvider.TYPE_IMAGES_ROOT.equals(rootId);
    }

    public boolean isVideos() {
        return MediaDocumentsProvider.AUTHORITY.equals(authority)
                && MediaDocumentsProvider.TYPE_VIDEOS_ROOT.equals(rootId);
    }

    public boolean isAudio() {
        return MediaDocumentsProvider.AUTHORITY.equals(authority)
                && MediaDocumentsProvider.TYPE_AUDIO_ROOT.equals(rootId);
    }

    public boolean isDocument() {
        return NonMediaDocumentsProvider.AUTHORITY.equals(authority)
                && NonMediaDocumentsProvider.TYPE_DOCUMENT_ROOT.equals(rootId);
    }

    public boolean isArchive() {
        return NonMediaDocumentsProvider.AUTHORITY.equals(authority)
                && NonMediaDocumentsProvider.TYPE_ARCHIVE_ROOT.equals(rootId);
    }

    public boolean isApk() {
        return NonMediaDocumentsProvider.AUTHORITY.equals(authority)
                && NonMediaDocumentsProvider.TYPE_APK_ROOT.equals(rootId);
    }






    public boolean isUsbStorage() {
        return UsbStorageProvider.AUTHORITY.equals(authority);
    }
    
    public boolean isEditSupported() {
        return (flags & Root.FLAG_SUPPORTS_EDIT) != 0;
    }

    public Uri getUri() {
        return DocumentsContract.buildRootUri(authority, rootId);
    }

    public boolean isMtp() {
        return "com.android.mtp.documents".equals(authority);
    }

    public boolean hasSettings() {
        return (flags & Root.FLAG_HAS_SETTINGS) != 0;
    }
    public boolean supportsChildren() {
        return (flags & Root.FLAG_SUPPORTS_IS_CHILD) != 0;
    }
    public boolean supportsCreate() {
        return (flags & Root.FLAG_SUPPORTS_CREATE) != 0;
    }
    public boolean supportsRecents() {
        return (flags & Root.FLAG_SUPPORTS_RECENTS) != 0;
    }
    public boolean supportsSearch() {
        return (flags & Root.FLAG_SUPPORTS_SEARCH) != 0;
    }
    public boolean isAdvanced() {
        return (flags & Root.FLAG_ADVANCED) != 0;
    }
    public boolean isLocalOnly() {
        return (flags & Root.FLAG_LOCAL_ONLY) != 0;
    }

    public boolean isEmpty() {
        return (flags & Root.FLAG_EMPTY) != 0;
    }

    public boolean isSd() {
        return (flags & Root.FLAG_REMOVABLE_SD) != 0;
    }

    public boolean isUsb() {
        return (flags & Root.FLAG_REMOVABLE_USB) != 0;
    }

    public boolean isServer() {
        return (flags & Root.FLAG_CONNECTION_SERVER) != 0;
    }



    @Override
    public boolean equals(Object o) {
        if (o instanceof RootInfo) {
            final RootInfo root = (RootInfo) o;
            return Objects.equals(authority, root.authority) && Objects.equals(rootId, root.rootId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(authority, rootId);
    }

    public String getDirectoryString() {
        return !TextUtils.isEmpty(summary) ? summary : title;
    }

    /**
     * Checks if the path contains any of the given tags or not
     *
     * @param path The Folder path
     * @param tags the list of tags to check against
     * @return true if path has atleast one tag matched else false
     */
    public boolean contains(String path, String... tags) {
        if(!TextUtils.isEmpty(path)){
            for (String tag : tags){
                if(path.toLowerCase().contains(tag)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Root{"
                + "authority=" + authority
                + ", rootId=" + rootId
                + ", documentId=" + documentId
                + ", path=" + path
                + ", title=" + title
                + ", isUsb=" + isUsb()
                + ", isSd=" + isSd()
                + ", isMtp=" + isMtp()
                + "}";
    }



}