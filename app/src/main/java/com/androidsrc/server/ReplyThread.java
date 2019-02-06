package com.androidsrc.server;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

public class ReplyThread extends Thread {

    private Socket hostThreadSocket;
    private String message1;
    private String message;
    private ObjectOutputStream objectOutputStream;

    ReplyThread(Socket socket, String message) {
        hostThreadSocket = socket;
        this.message1 = message;
    }

    // IMPORTANT: Send Objects using ObjectOutputStream class!!!

    @Override
    public void run() {
        OutputStream outputStream;
        try {
            outputStream = hostThreadSocket.getOutputStream();
//            objectOutputStream = new ObjectOutputStream(outputStream);
//            objectOutputStream.writeObject(message);
            PrintStream printStream = new PrintStream(outputStream);
            printStream.print(message1);
            Log.v("ERROR in SOCKET",printStream.checkError() + "");
            //printStream.close();

            message += "replayed: " + message1 + "\n";

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            message += "Something wrong! " + e.toString() + "\n";
        }
    }

}
