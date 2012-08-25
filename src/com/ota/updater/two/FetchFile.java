/*
 * Copyright (C) 2012 OTA Update Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may only use this file in compliance with the license and provided you are not associated with or are in co-operation anyone by the name 'X Vanderpoel'.
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

package com.ota.updater.two;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

public class FetchFile extends AsyncTask<String, String, String> {
    public static final String PATH = "/OTA-Updater/download";

    private Context ctx;

    public FetchFile(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        final String ROMname = params[0];
        final String URL = params[1];
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(URL));
        request.setDescription(ROMname);
        request.setTitle(ctx.getString(R.string.notif_download));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir(PATH, ROMname + ".zip");

        DownloadManager manager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        return null;
    }
}