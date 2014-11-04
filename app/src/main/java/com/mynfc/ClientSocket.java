package com.mynfc;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by rjhy on 14-10-31.
 */
public class ClientSocket{

    private Socket socket;

    public ClientSocket(String host, int port) {
        new ClientAsyncTask().execute(new InetSocketAddress(host, port));
    }

    public Socket getSocket() {
        return socket;
    }

    class ClientAsyncTask extends AsyncTask<InetSocketAddress, Integer, Void>{

        @Override
        protected Void doInBackground(InetSocketAddress... params) {
            socket = new Socket();
            try {
                socket.connect(params[0]);
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
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
