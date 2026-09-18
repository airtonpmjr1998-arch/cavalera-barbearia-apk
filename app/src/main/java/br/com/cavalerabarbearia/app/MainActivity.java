package br.com.cavalerabarbearia.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressBar progressBar;
    private View errorPanel;
    private TextView errorTitle;
    private TextView errorMessage;

    private String startUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(0xFF08090B);
        getWindow().setNavigationBarColor(0xFF08090B);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        errorPanel = findViewById(R.id.errorPanel);
        errorTitle = findViewById(R.id.errorTitle);
        errorMessage = findViewById(R.id.errorMessage);
        Button retryButton = findViewById(R.id.retryButton);

        startUrl = BuildConfig.WEB_APP_URL == null
                ? ""
                : BuildConfig.WEB_APP_URL.trim();

        retryButton.setOnClickListener(v -> loadStartPage());

        configureWebView();

        if (!isValidStartUrl(startUrl)) {
            showConfigurationError();
            return;
        }

        if (savedInstanceState == null) {
            loadStartPage();
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        String userAgent = settings.getUserAgentString();

        if (userAgent != null && !userAgent.contains("CavaleraBarbeariaAPK")) {
            settings.setUserAgentString(userAgent + " CavaleraBarbeariaAPK/1.0");
        }

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new CavaleraWebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(
                        newProgress >= 100 ? View.GONE : View.VISIBLE
                );
            }

            @Override
            public boolean onCreateWindow(
                    WebView view,
                    boolean isDialog,
                    boolean isUserGesture,
                    Message resultMsg
            ) {
                WebView popup = new WebView(MainActivity.this);

                popup.setWebViewClient(new WebViewClient() {
                    private boolean handled = false;

                    private boolean handle(Uri uri) {
                        if (handled || uri == null) {
                            return true;
                        }

                        handled = true;
                        handlePopupUri(uri);

                        popup.stopLoading();
                        popup.destroy();

                        return true;
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            WebResourceRequest request
                    ) {
                        return handle(request.getUrl());
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            String url
                    ) {
                        return handle(Uri.parse(url));
                    }

                    @Override
                    public void onPageStarted(
                            WebView view,
                            String url,
                            Bitmap favicon
                    ) {
                        handle(Uri.parse(url));
                    }
                });

                WebView.WebViewTransport transport =
                        (WebView.WebViewTransport) resultMsg.obj;

                transport.setWebView(popup);
                resultMsg.sendToTarget();

                return true;
            }
        });
    }

    private void loadStartPage() {
        if (!isValidStartUrl(startUrl)) {
            showConfigurationError();
            return;
        }

        hideError();
        progressBar.setVisibility(View.VISIBLE);
        webView.loadUrl(startUrl);
    }

    private boolean isValidStartUrl(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        Uri uri = Uri.parse(value);

        return "https".equalsIgnoreCase(uri.getScheme())
                && uri.getHost() != null
                && !"example.invalid".equalsIgnoreCase(uri.getHost());
    }

    private boolean isGoogleAppsScriptHost(Uri uri) {
        if (uri == null || uri.getHost() == null) {
            return false;
        }

        String host = uri.getHost().toLowerCase(Locale.ROOT);

        return host.equals("script.google.com")
                || host.endsWith(".script.google.com")
                || host.equals("script.googleusercontent.com")
                || host.endsWith(".script.googleusercontent.com");
    }

    private boolean shouldOpenExternal(Uri uri) {
        if (uri == null) {
            return false;
        }

        String scheme = lower(uri.getScheme());
        String host = lower(uri.getHost());

        if (
                scheme.equals("tel")
                        || scheme.equals("mailto")
                        || scheme.equals("sms")
                        || scheme.equals("smsto")
                        || scheme.equals("geo")
                        || scheme.equals("intent")
                        || scheme.equals("whatsapp")
        ) {
            return true;
        }

        if (!scheme.equals("http") && !scheme.equals("https")) {
            return true;
        }

        if (
                host.equals("wa.me")
                        || host.endsWith(".wa.me")
                        || host.equals("api.whatsapp.com")
                        || host.equals("web.whatsapp.com")
                        || host.endsWith(".whatsapp.com")
        ) {
            return true;
        }

        if (
                host.equals("maps.google.com")
                        || host.equals("maps.app.goo.gl")
        ) {
            return true;
        }

        if (host.equals("google.com") || host.equals("www.google.com")) {
            String path = uri.getPath() == null ? "" : uri.getPath();

            if (path.contains("/maps")) {
                return true;
            }
        }

        return !isGoogleAppsScriptHost(uri);
    }

    private void handlePopupUri(Uri uri) {
        if (uri == null) {
            return;
        }

        if (isGoogleAppsScriptHost(uri)) {
            webView.loadUrl(uri.toString());
            return;
        }

        openExternal(uri);
    }

    private void openExternal(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(intent);
        } catch (ActivityNotFoundException error) {
            Toast.makeText(
                    this,
                    "Nenhum aplicativo disponível para abrir este link.",
                    Toast.LENGTH_LONG
            ).show();
        } catch (Exception error) {
            Toast.makeText(
                    this,
                    "Não foi possível abrir este link.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void showNetworkError() {
        errorTitle.setText(R.string.error_title);
        errorMessage.setText(R.string.error_message);
        errorPanel.setVisibility(View.VISIBLE);
        webView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void showConfigurationError() {
        errorTitle.setText(R.string.config_error_title);
        errorMessage.setText(R.string.config_error_message);
        errorPanel.setVisibility(View.VISIBLE);
        webView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void hideError() {
        errorPanel.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private final class CavaleraWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view,
                WebResourceRequest request
        ) {
            Uri uri = request.getUrl();

            if (shouldOpenExternal(uri)) {
                openExternal(uri);
                return true;
            }

            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view,
                String url
        ) {
            Uri uri = Uri.parse(url);

            if (shouldOpenExternal(uri)) {
                openExternal(uri);
                return true;
            }

            return false;
        }

        @Override
        public void onPageStarted(
                WebView view,
                String url,
                Bitmap favicon
        ) {
            hideError();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(
                WebView view,
                WebResourceRequest request,
                WebResourceError error
        ) {
            if (request != null && request.isForMainFrame()) {
                showNetworkError();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
        }

        super.onDestroy();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }

        super.onBackPressed();
    }
}
