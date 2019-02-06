package com.androidsrc.server;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Server
{
	private WeakReference<TextView> message;
	private ServerSocket serverSocket;
	private String incomingMessage = "";
	private String outgoingMessage = "";
	private int socketServerPORT;
//	SocketServerReplyThread socketServerReplyThread;
	private Button send;
	private List<Socket> socketList = new ArrayList<Socket>();
	private int flag = 0;

	public Server(TextView msg) {
		message = new WeakReference<TextView>(msg);
		Thread socketServerThread = new Thread(new SocketServerThread());
		socketServerThread.start();
	}

	public int getPort() {
		return socketServerPORT;
	}

	//Destroy the client sockets and the serversocket.
	public void onDestroy() {
		if (serverSocket != null) {
			try {
				int i = 0;
				serverSocket.close();
				while(socketList.get(i) != null)
					socketList.get(i).close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	//Accept connections from clients and store their sockets.
	private class SocketServerThread extends Thread {

		int count = 0;

		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(0);
				socketServerPORT = serverSocket.getLocalPort();

				while (true) {
					Socket socket = serverSocket.accept();
					socketList.add(socket);
					count++;
					outgoingMessage += "#" + count + " from "
							+ socket.getInetAddress() + ":"
							+ socket.getPort() + "\n";

//					Handler mainHandler = new Handler(Looper.getMainLooper());
//					Runnable myRunnable = new Runnable() {
//						@Override
//						public void run() {
//							if(message.get() != null)
//							{
//								message.get().setText(outgoingMessage);
//							}
//						} // This is your code
//					};
//					mainHandler.post(myRunnable);

					displayMessage(outgoingMessage);

					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
					byte[] buffer = new byte[1024];
					int bytesRead;
					InputStream inputStream = socket.getInputStream();

					/*
					 * notice: inputStream.read() will block if no data return
					 */

					if((bytesRead = inputStream.read(buffer)) != -1)
					{
						byteArrayOutputStream.write(buffer, 0, bytesRead);
						incomingMessage = byteArrayOutputStream.toString("UTF-8");
						displayMessage(incomingMessage);
						new MessageReader(byteArrayOutputStream,inputStream,message.get()).start();
					}

//					socketServerReplyThread = new SocketServerReplyThread(
//							socket, count);
//					socketServerReplyThread.run();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void displayMessage(final String reply)
		{
			Handler mainHandler = new Handler(Looper.getMainLooper());
			Runnable myRunnable = new Runnable() {
				@Override
				public void run() {
					if(message.get() != null)
					{
						message.get().setText(reply);
					}
				} // This is your code
			};
			mainHandler.post(myRunnable);
		}

	}

	//Thread to send messages to clients.
//	private class SocketServerReplyThread extends Thread {
//
//		private Socket hostThreadSocket;
//		int cnt;
//
//		SocketServerReplyThread(Socket socket, int c) {
//			hostThreadSocket = socket;
//			cnt = c;
//		}
//
//		@Override
//		public void run() {
//			OutputStream outputStream;
//			String msgReply = "Hello from Server, you are #" + cnt;
//			try {
//				outputStream = hostThreadSocket.getOutputStream();
//				PrintStream printStream = new PrintStream(outputStream);
//				while(flag != 2) {
//					printStream.print(msgReply);
//					flag ++;
//				}
//				//printStream.close();
//
//				outgoingMessage += "replayed: " + msgReply + "\n";
//
//				activity.runOnUiThread(new Runnable() {
//
//					@Override
//					public void run() {
//						activity.msg.setText(outgoingMessage);
//					}
//				});
//
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				outgoingMessage += "Something wrong! " + e.toString() + "\n";
//			}
//
////			activity.runOnUiThread(new Runnable() {
////
////				@Override
////				public void run() {
////					activity.msg.setText(outgoingMessage);
////				}
////			});
//		}
//
//	}

	public List<Socket> getSockets()
	{
		return socketList;
	}

	public String getIpAddress() {
		String ip = "";
		try {
			Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
					.getNetworkInterfaces();
			while (enumNetworkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = enumNetworkInterfaces
						.nextElement();
				Enumeration<InetAddress> enumInetAddress = networkInterface
						.getInetAddresses();
				while (enumInetAddress.hasMoreElements()) {
					InetAddress inetAddress = enumInetAddress
							.nextElement();

					if (inetAddress.isSiteLocalAddress()) {
						ip += "Server running at : "
								+ inetAddress.getHostAddress();
					}
				}
			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ip += "Something Wrong! " + e.toString() + "\n";
		}
		return ip;
	}
}
