package com.mynfc;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by rjhy on 14-10-31.
 */
public class ServerIntentService extends IntentService {

    public ServerIntentService() {
        super("server");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
