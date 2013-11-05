package com.yuniz.wifispychecker;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import com.revmob.RevMob;
import com.revmob.RevMobTestingMode;
import com.yuniz.wifispychecker.R;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("NewApi")
public class WifiSpyChecker extends Activity {

	private TextView statusLogs;
	private TextView editText1;
	private Button scanBtn;
	
	private TextView textView4;
	//private EditText editText2;
	
	private int currentScannedIp1 = 127;
	private int currentScannedIp2 = 0;
	private int currentScannedIp3 = 0;
	private int currentScannedIp4 = 1;
	
	private int pcsOnline = 0;
	
	private LinearLayout ipsList;  //parent view where to add
	
	Timer WFT = new Timer();
	
	private RevMob revmob;
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		 StrictMode.setThreadPolicy(policy);
		
		setContentView(R.layout.activity_wifi_spy_checker);
		
		statusLogs = (TextView) findViewById(R.id.statusLogs);
		editText1 = (TextView) findViewById(R.id.textView3);
		//editText2 = (EditText) findViewById(R.id.editText2);
		ipsList = (LinearLayout) findViewById(R.id.ipsList);
		scanBtn = (Button) findViewById(R.id.button1);

		textView4 = (TextView) findViewById(R.id.textView4);
		
		editText1.setText(getIpAddr());
		
		/*----RevMob Ads----*/
		revmob = RevMob.start(this);
//revmob.setTestingMode(RevMobTestingMode.WITH_ADS);
		revmob.showFullscreen(this);
        /*----RevMob Ads----*/
	}

	public void updateStatus(){
		int percentCount = currentScannedIp4 * 100 / 255;
		statusLogs.setText("Scanning ... " + percentCount + "%");
		textView4.setText(pcsOnline + " devices found");
	}
	
	public void onScanNow(View v){

		if(scanBtn.getText().toString() == "STOP"){
			currentScannedIp4 = 257;
			scanBtn.setText("SCAN");
			revmob.showFullscreen(this);
		}else{
			pcsOnline=0;
		    ipsList.removeAllViewsInLayout();
			
			String hostName=editText1.getText().toString();
			String[] hostnameArray = hostName.split("[.]");
			
			currentScannedIp1 = Integer.parseInt(hostnameArray[0]);
			currentScannedIp2 = Integer.parseInt(hostnameArray[1]);
			currentScannedIp3 = Integer.parseInt(hostnameArray[2]);
			
			currentScannedIp4 = 1;
			scanBtn.setText("STOP");
			
			setWFT();
		}
	}
	
	public String getIpAddr() {
	   WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
	   WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	   int ip = wifiInfo.getIpAddress();

	   String ipString = String.format(
	   "%d.%d.%d.%d",
	   (ip & 0xff),
	   (ip >> 8 & 0xff),
	   (ip >> 16 & 0xff),
	   (ip >> 24 & 0xff));

	   return ipString;
	}
	
	public void checkIPAddress(){
		if(currentScannedIp4 > 255){
			scanBtn.setText("SCAN");
			pcsOnline=0;
			
			revmob.showFullscreen(this);
			
			return;
		}
		
		String hostName=editText1.getText().toString();
		String ipChildInfo = "";

		try {
			hostName = InetAddress.getByAddress(new byte[] { (byte) currentScannedIp1, (byte) currentScannedIp2, (byte)currentScannedIp3, (byte)currentScannedIp4 }).getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.v("debug", "X - " + hostName);
		ipChildInfo = hostName;
		
		try {
			InetAddress inet = InetAddress.getByName(hostName);

			try {
				Log.v("debug", "Y - " + inet.isReachable(500));
				if(inet.isReachable(500) == false){
					ipChildInfo="";
				}else{
					pcsOnline++;
					if(hostName.toString() == editText1.getText().toString()){
						ipChildInfo = pcsOnline + ") " + ipChildInfo + " [ ME ]";
					}else if(pcsOnline == 1){
						ipChildInfo = pcsOnline + ") " + ipChildInfo + " [ WIFI Router ]";
					}else{
						ipChildInfo = pcsOnline + ") " + ipChildInfo;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(ipChildInfo != ""){
			TextView ipListChild=new TextView(this);
			ipListChild.setText(ipChildInfo);
			ipListChild.setTextSize(14);
			ipListChild.setTextColor(Color.parseColor("#7e7e7e"));
		    ipsList.addView(ipListChild);
		}

		updateStatus();
		currentScannedIp4++;
		
		setWFT();
	}
	
	public void setWFT() {
        WFT.schedule(new TimerTask() {          
            @Override
            public void run() {
                WFTTimerMethod();
            }
        }, 500); // 4 seconds delay
    }

    private void WFTTimerMethod() {
        this.runOnUiThread(Timer_Tick);
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
        	checkIPAddress();
        }
    };
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wifi_spy_checker, menu);
		return true;
	}

}
