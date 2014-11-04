package com.mynfc;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by rjhy on 14-10-31.
 */
public class ServerSocket {
    public static int PORT = 7474;
    private java.net.ServerSocket serverSocket;

    public Socket getSocket() {
        return socket;
    }

    private Socket socket;

    class ServerAsyncTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                serverSocket = new java.net.ServerSocket(PORT);
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
