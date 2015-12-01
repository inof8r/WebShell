package com.example.webshell;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import com.example.webshell.util.SystemUiHider;
import com.example.webshell.util.DeviceUuidFactory;

import com.example.webshell.R;
import com.example.webshell.MainActivity.JavaScriptInterface;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.http.SslError;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class NFCManager extends Activity {
	protected static final String APP_VERSION  = "1.0";
	private WebView webview;
	private static NfcAdapter mAdapter;
	private static PendingIntent mPendingIntent;
	private static IntentFilter[] mFilters;
	private static String[][] mTechLists;
	public String curMode = "read";	
	public int writeMode = 0; 
	public String curTagData = "Test"; 
	
	public String curTagDataBlock1 = "0000";
	public String curTagDataBlock2 = "0000";
	public String curTagDataBlock3 = "0000";
	public String curTagDataBlock4 = "0000";
	
	IntentFilter[] intentFiltersArray ;
	String[][] techListsArray ;
	PendingIntent pendingIntent ;
		
	private static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1',
		(byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
		(byte) '7', (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
		(byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F' };		

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
		setContentView(com.example.webshell.R.layout.tagdetails);
		
		this.webview = (WebView)findViewById(R.string.webview);

		mAdapter = NfcAdapter.getDefaultAdapter(this);
		
		
		
		IntentFilter tagdisc = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
	        ndef.addDataType("*/*");
	    } catch (MalformedMimeTypeException e) {
	        throw new RuntimeException("fail", e);
	    }

	    IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
	    try {
	        tech.addDataType("*/*");
	    } catch (MalformedMimeTypeException e) {
	        throw new RuntimeException("fail", e);
	    }
	
		mFilters = new IntentFilter[] { ndef, tech , tagdisc};

		mTechLists = new String[][] { new String[] { NfcA.class.getName(),
	            NfcB.class.getName(), NfcF.class.getName(),
	            NfcV.class.getName(), IsoDep.class.getName(),
	            MifareClassic.class.getName(),
	            MifareUltralight.class.getName(), Ndef.class.getName() } };

		if (savedInstanceState == null) {
			mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			Intent intent = getIntent();
			try {
				resolveIntent(intent);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				Log.i("NFCError", "UnsupportedEncodingException");
				e.printStackTrace();
			}
		       this.webview = (WebView)findViewById(R.string.webview);
		       
			      
		        WebSettings settings = webview.getSettings();
		        settings.setJavaScriptEnabled(true);
		      
		        webview.addJavascriptInterface(new JavaScriptInterface(this), "Android");	        
		        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		        final ProgressDialog progressBar = ProgressDialog.show(NFCManager.this, getString(R.string.app_name), getString(R.string.reading_tag));
		        webview.setWebViewClient(new WebViewClient() {
		            public boolean shouldOverrideUrlLoading(WebView view, String url) {
		                Log.i("WebviewMsg", "shouldOverrideUrlLoading");
		                view.loadUrl(url);
		                return true;
		            }
		           
		            public void onPageFinished(WebView view, String url) {
		                Log.i("WebviewMsg", "Finished loading URL: " +url);
		                if (progressBar.isShowing()) {
		                    progressBar.dismiss();
		               }
		            }

		        });
		}

	}
	public void getEndpoint(String endoint) {
		
		webview = (WebView)findViewById(R.string.webview);
        String backend_url = getString(R.string.app_url) + endoint;
        webview.clearCache(true);
        webview.loadUrl(backend_url);
        
	}	
	
		
	/*
	########################################################################################################################
	# resolveIntent
	########################################################################################################################
	 */
	public  void playSoundFile(String file) {
		Context context = NFCManager.this;
		MediaPlayer mp;
		AudioManager mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,5, 0);
		String thefile = "clockbell";
		if (file != "") {
			thefile = file;
		
		}
		 Resources res = this.getResources(); 
		 int resID = res.getIdentifier(thefile, "raw", this.getPackageName());
		
	    mp = MediaPlayer.create(context, resID);
	    mp.setOnCompletionListener(new OnCompletionListener() {
	
	        @Override
	        public void onCompletion(MediaPlayer mp) {
	            // TODO Auto-generated method stub
	            mp.reset();
	            mp.release();
	            mp=null;
	        }
	
	    });
	    mp.start(); 
	}
		public void resolveIntent(Intent intent) throws UnsupportedEncodingException {
			// Identification unique device ID
			DeviceUuidFactory TheUID = new DeviceUuidFactory(this);
			String UUIDVal = TheUID.getDeviceUuid().toString();
						
			String cardID;
			NFCManager.this.playSoundFile("");
			final ProgressDialog progressBar = ProgressDialog.show(NFCManager.this, getString(R.string.app_name), getString(R.string.reading_tag));
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			cardID = bin2hex(tag.getId());					
			getEndpoint("#/tags/" + cardID	+ "/register");
			if (progressBar.isShowing()) {
	            progressBar.dismiss();
	       }
	
			
		}	
		@Override
		public void onPause() {
			super.onPause();
			mAdapter.disableForegroundDispatch(NFCManager.this);
		}
		@Override
		public void onResume() {
			super.onResume();	
			mAdapter.enableForegroundDispatch(NFCManager.this, mPendingIntent, mFilters, mTechLists);
		}	
		
		@Override
		public void onNewIntent(Intent intent) {
			Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
			try {
				resolveIntent(intent);
			} catch (UnsupportedEncodingException e) {
				Toast.makeText(this, "Tag not captured!", Toast.LENGTH_LONG).show();
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		public class JavaScriptInterface {
	        Context mContext;

	        /** Instantiate the interface and set the context */
	        JavaScriptInterface(Context c) {
	            mContext = c;
	        }
	        
	        @JavascriptInterface
	        public void setNFCOperationMode(String mode) {        	
	        	writeMode = 0;
	        	if (mode.equals("write")) {
	        		writeMode = 1;        	
	        	}
	        }
	        
	        @JavascriptInterface
	        public void receiveMessage(String msg) {
	        	Toast.makeText(NFCManager.this,  msg, Toast.LENGTH_SHORT).show();        	
	        }
	        @JavascriptInterface
	        public void playSound(String msg) {
	        	NFCManager.this.playSoundFile(msg);
	        }
	        

	    }

		static String bin2hex(byte[] data) {
		    return String.format("%0" + (data.length * 2) + "X", new BigInteger(1,data));
		}	
		public static String getHexString(byte[] raw, int len) {
			byte[] hex = new byte[2 * len];
			int index = 0;
			int pos = 0;

			for (byte b : raw) {
				if (pos >= len)
					break;

				pos++;
				int v = b & 0xFF;
				hex[index++] = HEX_CHAR_TABLE[v >>> 4];
				hex[index++] = HEX_CHAR_TABLE[v & 0xF];
			}

			return new String(hex);
		}		 
}
