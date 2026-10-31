package com.scenarioos.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.Toast;
import java.io.OutputStream;
import java.io.IOException;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST = 101;
    private static final int SAVE_FILE_REQUEST = 102;
    private byte[] pendingSaveBytes;
    private String pendingSaveMime = "application/octet-stream";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(12, 18, 32));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(12, 18, 32));
        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                    FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                Intent intent = params.createIntent();
                try { startActivityForResult(intent, FILE_CHOOSER_REQUEST); }
                catch (Exception e) { filePathCallback = null; callback.onReceiveValue(null); return false; }
                return true;
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
    }

    private class AndroidBridge {
        @JavascriptInterface public void saveFile(String name, String mime, String base64) {
            byte[] decoded;
            try { decoded = Base64.decode(base64, Base64.DEFAULT); }
            catch (Exception e) { runOnUiThread(() -> Toast.makeText(MainActivity.this, "Could not prepare export file", Toast.LENGTH_LONG).show()); return; }
            runOnUiThread(() -> {
                pendingSaveBytes = decoded;
                pendingSaveMime = (mime == null || mime.isEmpty()) ? "application/octet-stream" : mime;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(pendingSaveMime);
                intent.putExtra(Intent.EXTRA_TITLE, name == null ? "scenarioos-export" : name);
                startActivityForResult(intent, SAVE_FILE_REQUEST);
            });
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (filePathCallback != null) {
                Uri[] results = (resultCode == RESULT_OK && data != null)
                    ? WebChromeClient.FileChooserParams.parseResult(resultCode, data) : null;
                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            }
        } else if (requestCode == SAVE_FILE_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null && pendingSaveBytes != null) {
                try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                    if (out != null) out.write(pendingSaveBytes);
                    Toast.makeText(this, "Export saved successfully", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Could not save export file", Toast.LENGTH_LONG).show();
                }
            }
            pendingSaveBytes = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (webView != null) { webView.removeJavascriptInterface("AndroidBridge"); webView.destroy(); }
        super.onDestroy();
    }
}
