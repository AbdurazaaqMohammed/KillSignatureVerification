package com.example.killsig;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import dalvik.system.DexClassLoader;

import java.io.File;
import java.lang.reflect.Method;

import io.github.abdurazaaqmohammed.plugins.ipc.PluginContracts;

public class KillSigActivity extends Activity {

    private Uri apkUri;
    private static DexClassLoader hybrid;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apkUri = getIntent().getData();
        String method = getIntent().getStringExtra("method");
        if (method == null || method.isEmpty()) method = "mt";
        ProgressDialog pd = new ProgressDialog(this, android.R.style.Theme_Material_Dialog_Alert);
        pd.setMessage("Working…");
        pd.setCancelable(false);
        try {
            pd.show();
        } catch (Throwable ignored) {
        }
        final ProgressDialog pdDialog = pd;
        final String mMethod = method;
        new Thread(() -> {
            try {
                ClassLoader loader = hybridLoader();
                Class<?> engine = loader.loadClass("com.example.killsig.KillEngine");
                Method m = engine.getMethod("kill",
                        android.content.Context.class,
                        Uri.class, String.class);
                String outName = (String) m.invoke(null, KillSigActivity.this, apkUri, mMethod);
                Intent data = new Intent();
                data.putExtra(PluginContracts.EXTRA_MESSAGE,
                        ("repairip".equals(mMethod) ? "Removed Pairip: " : "Killed signature: ") + outName);
                data.putExtra(PluginContracts.EXTRA_OUTPUT_NAME, outName);
                runOnUiThread(() -> {
                    pdDialog.dismiss();
                    setResult(RESULT_OK, data);
                    finish();
                });
            } catch (Throwable t) {
                runOnUiThread(() -> {
                    pdDialog.dismiss();
                    Toast.makeText(KillSigActivity.this, String.valueOf(t), Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED);
                    finish();
                });
            }
        }).start();
    }

    private synchronized ClassLoader hybridLoader() throws Exception {
        if (hybrid != null) return hybrid;
        PackageManager pm = getPackageManager();
        ApplicationInfo hostInfo = pm.getApplicationInfo("io.github.abdurazaaqmohammed.MPManager", 0);
        DexClassLoader hostLoader = new DexClassLoader(
                hostInfo.sourceDir,
                new File(getCodeCacheDir(), "dl_host").getAbsolutePath(),
                hostInfo.nativeLibraryDir,
                getClass().getClassLoader().getParent());
        hybrid = new DexClassLoader(
                getApplicationInfo().sourceDir,
                new File(getCodeCacheDir(), "dl_self").getAbsolutePath(),
                getApplicationInfo().nativeLibraryDir,
                hostLoader);
        return hybrid;
    }
}
