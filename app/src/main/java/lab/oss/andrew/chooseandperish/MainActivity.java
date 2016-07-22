package lab.oss.andrew.chooseandperish;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class MainActivity extends AppCompatActivity {

    private JSONObject jsonResponse = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void startWork(View view) {
        ArrayList<String> playerUrls = new ArrayList<String>();
        EditText urlEdit = (EditText) findViewById(R.id.playerOne);

    }

    public void resolveUrl(View view) {
        EditText urlEdit = (EditText) findViewById(R.id.vanityUrlEdit);
        String vanity = urlEdit.getText().toString();
        String url = "https://api.steampowered.com/ISteamUser/ResolveVanityURL/v1/"
                + "?key=2E2F2BEA53C917775B3AEA7548391CC3"
                + "&vanityurl=" + vanity
                + "&url_type=1";

        /*getSteamResponse(url, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                String steamID = result.optString("steamid");
                mTextDisplay.setText(steamID);
            }
        });*/
        new getResponseTask().execute(url);

    }

    /*public void getOwnedGames() {
        final TextView mTextDisplay = (TextView) findViewById(R.id.txtDisplay);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject object = (JSONObject) new JSONTokener(response.toString()).nextValue();
                            String steamResponse = object.getString("response");
                            object = (JSONObject) new JSONTokener(steamResponse).nextValue();
                            String gameCount = object.getString("game_count");
                            JSONArray gamesArray = object.getJSONArray("games");
                            JSONObject game = gamesArray.getJSONObject(0);
                            int appid = game.getInt("appid");
                            mTextDisplay.setText(gameCount);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Something helpful, maybe display the error
                    }
                });

        VolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }*/

    private void getSteamResponse(String url, final VolleyCallback callback) {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject object = response.getJSONObject("response");
                            callback.onSuccess(object);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Do something
                    }
                });
        VolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);

    }

    private void setJsonResponse(JSONObject object) {
        this.jsonResponse = object;
    }

    public interface VolleyCallback {
        void onSuccess(JSONObject result);
    }

    private class getResponseTask extends AsyncTask<String, Integer, JSONObject> {
        Context context = MainActivity.this;
        final TextView mTextDisplay = (TextView) findViewById(R.id.txtDisplay);

        protected JSONObject doInBackground(String... urls) {
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urls[0], null, future, future);
            VolleySingleton.getInstance(context).addToRequestQueue(request);
            JSONObject response = new JSONObject();

            try {
                response = future.get(10, TimeUnit.SECONDS);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }

            return response.optJSONObject("response");
        }

        protected void onPostExecute(JSONObject result) {
            String steamID = result.optString("steamid");
            mTextDisplay.setText(steamID);
        }
    }

}
