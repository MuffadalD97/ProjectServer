package com.androidsrc.server;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class MessageReader extends Thread {

    private byte[] buffer = new byte[1024];
    private int bytesRead;
    private ByteArrayOutputStream byteArrayOutputStream;
    private InputStream inputStream;
    private String response = "";
    private WeakReference<TextView> message;

    MessageReader(ByteArrayOutputStream byteArrayOutputStream, InputStream inputStream, TextView message)
    {
        this.byteArrayOutputStream = byteArrayOutputStream;
        this.inputStream = inputStream;
        this.message = new WeakReference<TextView>(message);
    }

    @Override
    public void run() {
        try {
            if((bytesRead = inputStream.read(buffer)) != -1)
            {
                byteArrayOutputStream.reset();
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response = byteArrayOutputStream.toString("UTF-8");
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        activity.msg.setText(activity.msg.getText() + "\n" + response);
//                    }
//                });
                Handler mainHandler = new Handler(Looper.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if(message.get() != null)
                        {
                            message.get().setText(message.get().getText() + "\n" + response);
                        }
                    } // This is your code
                };
                mainHandler.post(myRunnable);
                new MessageReader(byteArrayOutputStream,inputStream,message.get()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
