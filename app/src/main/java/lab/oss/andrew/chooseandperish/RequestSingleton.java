package lab.oss.andrew.chooseandperish;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by andreg2 on 7/19/16.
 */
public class RequestSingleton {
    private static RequestSingleton ourInstance;
    private RequestQueue mRequestQueue;
    private static Context rqCtx;

    private RequestSingleton(Context context) {
        rqCtx = context;
        mRequestQueue = getRequestQueue();

    }

    public static synchronized RequestSingleton getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new RequestSingleton(context);
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
