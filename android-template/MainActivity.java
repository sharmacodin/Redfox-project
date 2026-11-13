package com.mobile.studio.myapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private static final int AI_PERMISSION_REQUEST_CODE = 1001;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Proactively request runtime permissions required by AI voice mode, recording & camera
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] requiredPermissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.CAMERA
            };
            boolean needsPrompt = false;
            for (String perm : requiredPermissions) {
                if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                    needsPrompt = true;
                    break;
                }
            }
            if (needsPrompt) {
                ActivityCompat.requestPermissions(this, requiredPermissions, AI_PERMISSION_REQUEST_CODE);
            }
        }

        // 2. Configure WebView for voice WebRTC capture, Gboard clipboard paste, and automatic voice synthesis
        try {
            WebView webView = this.bridge != null ? this.bridge.getWebView() : null;
            if (webView != null) {
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setDatabaseEnabled(true);
                
                // Allow speech synthesis and audio to speak automatically without manual tap requirement
                settings.setMediaPlaybackRequiresUserGesture(false);
                
                // File & clipboard data access
                settings.setAllowFileAccess(true);
                settings.setAllowContentAccess(true);

                // Ensure focus for keyboard input and Gboard clipboard toolbar
                webView.setFocusable(true);
                webView.setFocusableInTouchMode(true);

                // WebChromeClient to grant HTML5 WebRTC Microphone & Camera capture
                final WebChromeClient customChromeClient = new WebChromeClient() {
                    @Override
                    public void onPermissionRequest(final PermissionRequest request) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Grant requested web resources (e.g. AUDIO_CAPTURE, VIDEO_CAPTURE)
                                request.grant(request.getResources());
                            }
                        });
                    }
                };
                webView.setWebChromeClient(customChromeClient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
