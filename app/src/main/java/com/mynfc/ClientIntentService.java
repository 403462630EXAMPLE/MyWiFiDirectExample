package com.mynfc;

import android.app.IntentService;
import android.content.Intent;

import java.net.Socket;

/**
 * Created by rjhy on 14-10-31.
 */
public class ClientIntentService extends IntentService {

    private ClientSocket clientSocket;

    public ClientIntentService() {
        super("client");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
