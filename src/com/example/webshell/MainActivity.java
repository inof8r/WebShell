package com.example.webshell;

import java.io.File;
import com.example.webshell.R;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
	protected static final String APP_VERSION  = "0.6";
	private WebView webview;
	public String curCardID = "";
	public String curMode = "read";	
	
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
	    handler.proceed(); // Ignore SSL certificate errors
	}
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(com.example.webshell.R.layout.main);

		this.webview = (WebView)findViewById(R.string.webview);

		if (savedInstanceState == null) {
		       
		        WebSettings settings = webview.getSettings();
		        settings.setJavaScriptEnabled(true);
		        
		        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		        final File destinationDir = new File (Environment.getExternalStorageDirectory(), getPackageName());
		        if (!destinationDir.exists()) {
		            destinationDir.mkdir();
		        }
		      
		        webview.addJavascriptInterface(new JavaScriptInterface(this), "Android");	        
		        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		        final ProgressDialog progressBar = ProgressDialog.show(MainActivity.this, getString(R.string.app_name), getString(R.string.app_loading));
		        webview.setWebViewClient(new WebViewClient() {
		            public boolean shouldOverrideUrlLoading(WebView view, String url) {
		                Log.i("WebviewMsg", "shouldOverrideUrlLoading");
		                if (url.endsWith(".apk")) {
		                    Uri source = Uri.parse(url);
		                    DownloadManager.Request request = new DownloadManager.Request(source);
		                    File destinationFile = new File (destinationDir, source.getLastPathSegment());
		                    request.setDestinationUri(Uri.fromFile(destinationFile));
		                    manager.enqueue(request);
		                } else {
		                	view.loadUrl(url);
		                }
		                return true;
		            }
		           
		            public void onPageFinished(WebView view, String url) {
		                Log.i("WebviewMsg", "Finished loading URL: " +url);
		                if (progressBar.isShowing()) {
		                    progressBar.dismiss();
		               }
		            }
		            		            
		        });		        
		        // load home endpoint
		        getEndpoint("/#/home");			
		}

	}
		
	public void getEndpoint(String endoint) {
        String backend_url = getString(R.string.app_url) + endoint;
        webview.clearCache(true);
        webview.loadUrl(backend_url);
	}	
			
	public class JavaScriptInterface {
        Context mContext;
        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void receiveMessage(String msg) {
        	Toast.makeText(MainActivity.this,  msg, Toast.LENGTH_SHORT).show();        	
        }	        
	}		
		 
}