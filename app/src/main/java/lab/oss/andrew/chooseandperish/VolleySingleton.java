/*
 * Copyright (c) 2016 Andrew Gonzalez
 * This code is available under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */

package lab.oss.andrew.chooseandperish;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by andreg2 on 7/19/16.
 */
public class VolleySingleton {
    private static VolleySingleton ourInstance;
    private RequestQueue mRequestQueue;
    private static Context rqCtx;

    private VolleySingleton(Context context) {
        rqCtx = context;
        mRequestQueue = getRequestQueue();

    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new VolleySingleton(context);
        }
        return ourInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(rqCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
