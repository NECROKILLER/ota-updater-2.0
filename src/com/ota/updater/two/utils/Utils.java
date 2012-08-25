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

package com.ota.updater.two.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ota.updater.two.R;
import com.ota.updater.two.TabDisplay;
import com.ota.updater.two.utils.ShellCommand.CommandResult;

public class Utils {
    private static String cachedRomID = null;
    private static Date cachedRomDate = null;
    private static String cachedRomVer = null;

    private static String cachedKernelID = null;
    private static Date cachedKernelDate = null;
    private static String cachedKernelVer = null;
    private static String cachedKernelUname = null;

    private static String cachedOSSdPath = null;
    private static String cachedRcvrySdPath = null;

    public static String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());

            return byteArrToStr(digest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String md5(File f) {
        InputStream in = null;
        try {
            in = new FileInputStream(f);

            MessageDigest digest = MessageDigest.getInstance("MD5");

            byte[] buf = new byte[4096];
            int nRead = -1;
            while ((nRead = in.read(buf)) != -1) {
                digest.update(buf, 0, nRead);
            }

            return byteArrToStr(digest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try { in.close(); }
                catch (IOException e) { }
            }
        }
        return "";
    }

    public static void toastWrapper(final Activity activity, final CharSequence text, final int duration) {
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, text, duration).show();
            }
        });
    }

    public static void toastWrapper(final Activity activity, final int resId, final int duration) {
        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(activity, resId, duration).show();
            }
        });
    }

    public static void toastWrapper(final View view, final CharSequence text, final int duration) {
        view.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(view.getContext(), text, duration).show();
            }
        });
    }

    public static void toastWrapper(final View view, final int resId, final int duration) {
        view.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(view.getContext(), resId, duration).show();
            }
        });
    }

    public static boolean marketAvailable(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        try {
            pm.getPackageInfo("com.android.vending", 0);
        } catch (NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public static boolean isRomOtaEnabled() {
        return new File("/system/rom.ota.prop").exists();
    }

    public static boolean isKernelOtaEnabled() {
        return new File("/system/kernel.ota.prop").exists();
    }

    public static String getRomOtaID() {
        if (!isRomOtaEnabled()) return null;
        if (cachedRomID == null) {
            readRomOtaProp();
        }
        return cachedRomID;
    }

    public static Date getRomOtaDate() {
        if (!isRomOtaEnabled()) return null;
        if (cachedRomDate == null) {
            readRomOtaProp();
        }
        return cachedRomDate;
    }

    public static String getRomOtaVersion() {
        if (!isRomOtaEnabled()) return null;
        if (cachedRomVer == null) {
            readRomOtaProp();
        }
        return cachedRomVer;
    }

    public static String getRomVersion() {
        ShellCommand cmd = new ShellCommand();
        CommandResult modversion = cmd.sh.runWaitFor("getprop ro.modversion");
        if (modversion.stdout.length() != 0) return modversion.stdout;

        CommandResult cmversion = cmd.sh.runWaitFor("getprop ro.cm.version");
        if (cmversion.stdout.length() != 0) return cmversion.stdout;

        CommandResult aokpversion = cmd.sh.runWaitFor("getprop ro.aokp.version");
        if (aokpversion.stdout.length() != 0) return aokpversion.stdout;

        return Build.DISPLAY;
    }

    public static String getKernelOtaID() {
        if (!isKernelOtaEnabled()) return null;
        if (cachedKernelID == null) {
            readKernelOtaProp();
        }
        return cachedKernelID;
    }

    public static Date getKernelOtaDate() {
        if (!isKernelOtaEnabled()) return null;
        if (cachedKernelDate == null) {
            readKernelOtaProp();
        }
        return cachedKernelDate;
    }

    public static String getKernelOtaVersion() {
        if (!isKernelOtaEnabled()) return null;
        if (cachedKernelVer == null) {
            readKernelOtaProp();
        }
        return cachedKernelVer;
    }

    public static String getKernelVersion() {
        if (cachedKernelUname == null) {
            ShellCommand cmd = new ShellCommand();
            CommandResult propResult = cmd.sh.runWaitFor("uname -r -v");
            if (propResult.stdout.length() == 0) return null;
            cachedKernelUname = propResult.stdout;
        }
        return cachedKernelUname;
    }

    public static String getOSSdPath() {
        if (cachedOSSdPath == null) {
            ShellCommand cmd = new ShellCommand();
            CommandResult propResult = cmd.sh.runWaitFor("getprop " + Config.OTA_SD_PATH_OS_PROP);
            if (propResult.stdout.length() == 0) return "sdcard";
            cachedOSSdPath = propResult.stdout;
        }
        return cachedOSSdPath;
    }

    public static String getRcvrySdPath() {
        if (cachedRcvrySdPath == null) {
            ShellCommand cmd = new ShellCommand();
            CommandResult propResult = cmd.sh.runWaitFor("getprop " + Config.OTA_SD_PATH_RECOVERY_PROP);
            if (propResult.stdout.length() == 0) return "sdcard";
            cachedRcvrySdPath = propResult.stdout;
        }
        return cachedRcvrySdPath;
    }

    private static void readRomOtaProp() {
        if (!isRomOtaEnabled()) return;

        ShellCommand cmd = new ShellCommand();
        CommandResult catResult = cmd.sh.runWaitFor("cat /system/rom.ota.prop");
        if (catResult.stdout.length() == 0) return;

        try {
            JSONObject romOtaProp = new JSONObject(catResult.stdout);
            cachedRomID = romOtaProp.getString("otaid");
            cachedRomVer = romOtaProp.getString("otaver");
            cachedRomDate = parseDate(romOtaProp.getString("otadate"));
        } catch (JSONException e) {
            Log.e(Config.LOG_TAG + "ReadOTAProp", "Error in rom.ota.prop file!");
        }
    }

    private static void readKernelOtaProp() {
        if (!isKernelOtaEnabled()) return;

        ShellCommand cmd = new ShellCommand();
        CommandResult catResult = cmd.sh.runWaitFor("cat /system/kernel.ota.prop");
        if (catResult.stdout.length() == 0) return;

        try {
            JSONObject romOtaProp = new JSONObject(catResult.stdout);
            cachedKernelID = romOtaProp.getString("otaid");
            cachedKernelVer = romOtaProp.getString("otaver");
            cachedKernelDate = parseDate(romOtaProp.getString("otadate"));
        } catch (JSONException e) {
            Log.e(Config.LOG_TAG + "ReadOTAProp", "Error in kernel.ota.prop file!");
        }
    }

    public static boolean dataAvailable(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    public static Date parseDate(String date) {
        if (date == null) return null;
        try {
            return new SimpleDateFormat("yyyyMMdd-kkmm").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatDate(Date date) {
        if (date == null) return null;
        return new SimpleDateFormat("yyyyMMdd-kkmm").format(date);
    }

    public static boolean isRomUpdate(RomInfo info) {
        if (info == null) return false;
        if (info.version != null) {
            if (getRomOtaVersion() == null || !info.version.equalsIgnoreCase(getRomOtaVersion())) return true;
        }
        if (info.date != null) {
            if (getRomOtaDate() == null || info.date.after(getRomOtaDate())) return true;
        }
        return false;
    }

    public static boolean isKernelUpdate(KernelInfo info) {
        if (info == null) return false;
        if (info.version != null) {
            if (getKernelOtaVersion() == null || !info.version.equalsIgnoreCase(getKernelOtaVersion())) return true;
        }
        if (info.date != null) {
            if (getKernelOtaDate() == null || info.date.after(getKernelOtaDate())) return true;
        }
        return false;
    }

    public static void showRomUpdateNotif(Context ctx, RomInfo info) {
        Intent i = new Intent(ctx, TabDisplay.class);
        i.setAction(TabDisplay.ROM_NOTIF_ACTION);
        info.addToIntent(i);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder = new Notification.Builder(ctx);
        builder.setContentIntent(contentIntent);
        builder.setContentTitle(ctx.getString(R.string.notif_source));
        builder.setContentText(ctx.getString(R.string.notif_text_rom));
        builder.setTicker(ctx.getString(R.string.notif_text_rom));
        builder.setWhen(System.currentTimeMillis());
        //builder.setSmallIcon(R.drawable.updates);
        nm.notify(1, builder.getNotification());
    }

    public static void showKernelUpdateNotif(Context ctx, KernelInfo info) {
        Intent i = new Intent(ctx, TabDisplay.class);
        i.setAction(TabDisplay.KERNEL_NOTIF_ACTION);
        info.addToIntent(i);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder = new Notification.Builder(ctx);
        builder.setContentIntent(contentIntent);
        builder.setContentTitle(ctx.getString(R.string.notif_source));
        builder.setContentText(ctx.getString(R.string.notif_text_kernel));
        builder.setTicker(ctx.getString(R.string.notif_text_kernel));
        builder.setWhen(System.currentTimeMillis());
        //builder.setSmallIcon(R.drawable.updates);
        nm.notify(1, builder.getNotification());
    }

    private static final char[] HEX_DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    public static String byteArrToStr(byte[] bytes) {
        StringBuffer str = new StringBuffer();
        for (int q = 0; q < bytes.length; q++) {
            str.append(HEX_DIGITS[(0xF0 & bytes[q]) >>> 4]);
            str.append(HEX_DIGITS[0xF & bytes[q]]);
        }
        return str.toString();
    }
}