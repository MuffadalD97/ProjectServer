package com.androidsrc.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.Socket;
import java.util.List;

public class MainActivity extends Activity {

	private Server server;
	private NsdManager mNsdManager;
	private String mServiceName;
	private int port;
	private List<Socket> socketList;
	private String message;
	private final String USERNAME = "USERNAME";
	private String username = "Username";
	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;

	private TextView infoip, msg;
	private Button send, set;
	private EditText editText, usernameEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		infoip = (TextView) findViewById(R.id.infoip);
		editText = (EditText) findViewById(R.id.text);
		msg = (TextView) findViewById(R.id.msg);
		send = (Button)findViewById(R.id.send);
		set = (Button) findViewById(R.id.save);
		usernameEditText = (EditText) findViewById(R.id.username);

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        username = sharedPreferences.getString(USERNAME,username);
        msg.setText(username);

		//onClickListener to set username of user
		set.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(usernameEditText.getText().toString() != "")
				{
					editor.putString(USERNAME,usernameEditText.getText().toString());
					editor.apply();
				}
			}
		});

		//onClickListener to send messages to client.
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				message = editText.getText().toString();
				editText.setText("");
				ReplyThread replyThread = new ReplyThread(server.getSockets().get(0),message);
				replyThread.start();
			}
		});

		//Thread to accept connections.
		server = new Server(msg);

		//display device info
		infoip.setText(server.getIpAddress()+":"+server.getPort());
		port = server.getPort();
		registerService(port);
	}

	//Register service for other devices to see.
	public void registerService(int port) {
		NsdServiceInfo serviceInfo = new NsdServiceInfo();
		serviceInfo.setServiceName("NsdChat");
		serviceInfo.setServiceType("_http._tcp.");
		serviceInfo.setPort(port);

		mNsdManager = (NsdManager) getApplicationContext().getSystemService(getApplicationContext().NSD_SERVICE);

		mNsdManager.registerService(
				serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
	}

	//Listener to check what happened to service registration.
	NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {

		@Override
		public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
			// Save the service name. Android may have changed it in order to
			// resolve a conflict, so update the name you initially requested
			// with the name Android actually used.
			mServiceName = NsdServiceInfo.getServiceName();
			Log.v("TAG","YOLYOLYOLYO" + port);
		}

		@Override
		public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
			// Registration failed! Put debugging code here to determine why.
		}

		@Override
		public void onServiceUnregistered(NsdServiceInfo arg0) {
			// Service has been unregistered. This only happens when you call
			// NsdManager.unregisterService() and pass in this listener.
		}

		@Override
		public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
			// Unregistration failed. Put debugging code here to determine why.
		}
	};

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
        mNsdManager.unregisterService(mRegistrationListener);
		server.onDestroy();
	}


}