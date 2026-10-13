package com.example.killsig;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.abdurazaaqmohammed.utils.PairipRemoverUtil;
import io.github.abdurazaaqmohammed.utils.SignatureKillerUtil;

public class KillEngine {

    public static String kill(Context context, Uri stagedUri, String method) throws Exception {
        File in = copyUriToCache(context, stagedUri);
        File result = "repairip".equals(method)
                ? PairipRemoverUtil.removePairip(context, in)
                : SignatureKillerUtil.apply(context, in);
        writeOverUri(context, stagedUri, result);
        return result.getName();
    }

    private static File copyUriToCache(Context context, Uri uri) throws Exception {
        File out = new File(context.getCacheDir(), cleanName(uri));
        try (InputStream in = context.getContentResolver().openInputStream(uri);
             OutputStream o = new FileOutputStream(out)) {
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) != -1) o.write(buf, 0, n);
        }
        return out;
    }

    private static void writeOverUri(Context context, Uri uri, File src) throws Exception {
        try (InputStream in = new FileInputStream(src);
             OutputStream o = context.getContentResolver().openOutputStream(uri, "wt")) {
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) != -1) o.write(buf, 0, n);
        }
    }

    private static String cleanName(Uri uri) {
        String last = uri.getLastPathSegment();
        return last == null ? "input.apk" : last.replace('/', '_');
    }
}
