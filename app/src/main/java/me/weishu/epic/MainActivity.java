package me.weishu.epic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by weishu on 17/3/15.
 */

public class MainActivity extends Activity {

    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        Button test = new Button(this);
        test.setText("show hello world!");
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helloWorld();
            }
        });

        Button hook = new Button(this);
        hook.setText("hook");
        hook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Method origin = MainActivity.class.getDeclaredMethod("helloWorld");
                    Method replace = MainActivity.class.getDeclaredMethod("helloArt");
                    Hook.hook(origin, replace);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        Button loadUrl = new Button(this);
        loadUrl.setText("load url");
        loadUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.loadUrl("http://www.baidu.com");
            }
        });

        Button hookLoadUrl = new Button(this);
        hookLoadUrl.setText("hook loadUrl");
        hookLoadUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Method origin = WebView.class.getDeclaredMethod("loadUrl", String.class);
                    Method replace = MainActivity.class.getDeclaredMethod("interceptLoadUrl");
                    Hook.hook(origin, replace);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        mWebView = new WebView(this);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());

        layout.addView(test);
        layout.addView(hook);
        layout.addView(loadUrl);
        layout.addView(hookLoadUrl);
        layout.addView(mWebView);
        setContentView(layout);
    }

    public void helloWorld() {
        Toast.makeText(this, "hello world!", Toast.LENGTH_SHORT).show();
    }

    public void helloArt() {
        Toast.makeText(this, "hello art!", Toast.LENGTH_SHORT).show();
        Hook.callOrigin(this);
    }

    public void interceptLoadUrl() {
        // 可以理解为，现在这个方法直接就在WebView这个类里面；
        Hook.callOrigin(this, "http://www.so.com");
    }
}
