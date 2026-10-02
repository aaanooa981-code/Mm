package com.morgoo.helper.compat;

import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Najm Space compatibility replacement for DroidPlugin native-library extraction.
 *
 * DroidPlugin's original implementation calls hidden framework APIs from
 * com.android.internal.content.NativeLibraryHelper. On Android 9+ those calls can
 * be blocked by hidden-API enforcement and DroidPlugin reports that failure as
 * INSTALL_FAILED_NOT_SUPPORT_ABI (-3), even when the CPU ABI is actually valid.
 *
 * This implementation avoids hidden framework APIs and extracts the matching
 * native libraries directly from the plugin APK.
 */
public final class NativeLibraryHelperCompat {
    private static final String TAG = "NajmNativeLibs";

    private NativeLibraryHelperCompat() {}

    public static int copyNativeBinaries(File apkFile, File sharedLibraryDir) {
        ZipFile zip = null;
        try {
            if (apkFile == null || !apkFile.isFile()) return -1;
            if (sharedLibraryDir == null) return -1;
            if (!sharedLibraryDir.exists() && !sharedLibraryDir.mkdirs()) return -1;

            zip = new ZipFile(apkFile);
            Set<String> apkAbis = collectApkAbis(zip);
            if (apkAbis.isEmpty()) {
                Log.i(TAG, "APK has no native libraries: " + apkFile.getName());
                return 0;
            }

            boolean process64 = is64BitProcess();
            List<String> candidates = processAbiCandidates(process64);
            String selectedAbi = null;
            for (String abi : candidates) {
                if (abi != null && apkAbis.contains(abi)) {
                    selectedAbi = abi;
                    break;
                }
            }

            Log.i(TAG, "process64=" + process64 + ", apkAbis=" + apkAbis + ", candidates=" + candidates + ", selected=" + selectedAbi);
            if (selectedAbi == null) return -1;

            String prefix = "lib/" + selectedAbi + "/";
            Enumeration<? extends ZipEntry> entries = zip.entries();
            boolean copiedAny = false;
            byte[] buffer = new byte[32768];

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (name.contains("../") || !name.startsWith(prefix) || !name.endsWith(".so")) continue;

                String fileName = name.substring(prefix.length());
                if (fileName.length() == 0 || fileName.contains("/")) continue;

                File out = new File(sharedLibraryDir, fileName);
                InputStream in = null;
                FileOutputStream fos = null;
                try {
                    in = zip.getInputStream(entry);
                    fos = new FileOutputStream(out, false);
                    int n;
                    while ((n = in.read(buffer)) > 0) fos.write(buffer, 0, n);
                    fos.flush();
                    out.setReadable(true, false);
                    out.setExecutable(true, false);
                    copiedAny = true;
                } finally {
                    if (fos != null) try { fos.close(); } catch (Exception ignored) {}
                    if (in != null) try { in.close(); } catch (Exception ignored) {}
                }
            }

            return copiedAny ? 0 : -1;
        } catch (Throwable e) {
            Log.e(TAG, "Native library extraction failed", e);
            return -1;
        } finally {
            if (zip != null) try { zip.close(); } catch (Exception ignored) {}
        }
    }

    private static Set<String> collectApkAbis(ZipFile zip) {
        Set<String> result = new HashSet<String>();
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) continue;
            String name = entry.getName();
            if (name.contains("../") || !name.startsWith("lib/") || !name.endsWith(".so")) continue;
            int start = 4;
            int slash = name.indexOf('/', start);
            if (slash > start) result.add(name.substring(start, slash));
        }
        return result;
    }

    private static boolean is64BitProcess() {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                return android.os.Process.is64Bit();
            } catch (Throwable ignored) {}
        }
        String arch = System.getProperty("os.arch", "");
        return arch != null && arch.contains("64");
    }

    private static List<String> processAbiCandidates(boolean process64) {
        List<String> result = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= 21) {
            String[] abis = process64 ? Build.SUPPORTED_64_BIT_ABIS : Build.SUPPORTED_32_BIT_ABIS;
            if (abis != null) {
                for (String abi : abis) if (abi != null && abi.length() > 0) result.add(abi);
            }
        } else {
            if (Build.CPU_ABI != null && Build.CPU_ABI.length() > 0) result.add(Build.CPU_ABI);
            if (Build.CPU_ABI2 != null && Build.CPU_ABI2.length() > 0 && !result.contains(Build.CPU_ABI2)) result.add(Build.CPU_ABI2);
        }
        return result;
    }
}
