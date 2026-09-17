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
    private Button retryButton;

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
        retryButton = findViewById(R.id.retryButton);

        startUrl = BuildConfig.WEB_APP_URL == null
                ? ""
                : BuildConfig.WEB_APP_URL.trim();

        retryButton.setOnClickListener(v -> loadStartPage());

        configureWebView();

        if (!isValidWebAppUrl(startUrl)) {
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

        String currentUserAgent = settings.getUserAgentString();

        if (currentUserAgent != null &&
                !currentUserAgent.contains("CavaleraBarbeariaAPK")) {
            settings.setUserAgentString(
                    currentUserAgent + " CavaleraBarbeariaAPK/1.0"
            );
        }

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new CavaleraWebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);

                if (newProgress >= 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public boolean onCreateWindow(
                    WebView view,
                    boolean isDialog,
                    boolean isUserGesture,
                    Message resultMsg
            ) {
                final WebView popupWebView =
                        new WebView(MainActivity.this);

                popupWebView.setWebViewClient(new WebViewClient() {
                    private boolean handled = false;

                    private boolean handleOnce(Uri uri) {
                        if (handled || uri == null) {
                            return true;
                        }

                        handled = true;
                        handlePopupUri(uri);
                        popupWebView.stopLoading();
                        popupWebView.destroy();
                        return true;
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            WebResourceRequest request
                    ) {
                        return handleOnce(request.getUrl());
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            String url
                    ) {
                        return handleOnce(Uri.parse(url));
                    }

                    @Override
                    public void onPageStarted(
                            WebView view,
                            String url,
                            Bitmap favicon
                    ) {
                        handleOnce(Uri.parse(url));
                    }
                });

                WebView.WebViewTransport transport =
                        (WebView.WebViewTransport) resultMsg.obj;

                transport.setWebView(popupWebView);
                resultMsg.sendToTarget();

                return true;
            }
        });
    }

    private void loadStartPage() {
        if (!isValidWebAppUrl(startUrl)) {
            showConfigurationError();
            return;
        }

        hideError();
        progressBar.setVisibility(View.VISIBLE);
        webView.loadUrl(startUrl);
    }

    private boolean isValidWebAppUrl(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        Uri uri = Uri.parse(value);

        return "https".equalsIgnoreCase(uri.getScheme())
                && uri.getHost() != null
                && !uri.getHost().equalsIgnoreCase("example.invalid");
    }

    private boolean isInternalWebAppUri(Uri uri) {
        if (uri == null) {
            return false;
        }

        String scheme = safeLower(uri.getScheme());
        String host = safeLower(uri.getHost());

        if (!"https".equals(scheme) && !"http".equals(scheme)) {
            return false;
        }

        return host.equals("script.google.com")
                || host.endsWith(".script.google.com")
                || host.equals("script.googleusercontent.com")
                || host.endsWith(".script.googleusercontent.com");
    }

    private boolean shouldOpenExternally(Uri uri) {
        if (uri == null) {
            return false;
        }

        String scheme = safeLower(uri.getScheme());
        String host = safeLower(uri.getHost());

        if (
                scheme.equals("tel")
                        || scheme.equals("mailto")
                        || scheme.equals("sms")
                        || scheme.equals("smsto")
                        || scheme.equals("geo")
                        || scheme.equals("intent")
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
                        || host.endsWith(".whatsapp.com")
                        || host.equals("whatsapp.com")
        ) {
            return true;
        }

        if (
                host.equals("maps.google.com")
                        || host.equals("www.google.com")
                        || host.equals("google.com")
                        || host.equals("maps.app.goo.gl")
        ) {
            String path = uri.getPath() == null ? "" : uri.getPath();
            String query = uri.getQuery() == null ? "" : uri.getQuery();

            return path.contains("maps")
                    || query.contains("query=Cavalera");
        }

        return !isInternalWebAppUri(uri);
    }

    private void handlePopupUri(Uri uri) {
        if (uri == null) {
            return;
        }

        if (isInternalWebAppUri(uri)) {
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

    private void showNetworkError(String detail) {
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

    private String safeLower(String value) {
        return value == null
                ? ""
                : value.toLowerCase(Locale.ROOT);
    }

    private final class CavaleraWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view,
                WebResourceRequest request
        ) {
            Uri uri = request.getUrl();

            if (shouldOpenExternally(uri)) {
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

            if (shouldOpenExternally(uri)) {
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
                showNetworkError(
                        error == null
                                ? ""
                                : String.valueOf(error.getDescription())
                );
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
