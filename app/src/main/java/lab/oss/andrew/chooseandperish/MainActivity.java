package lab.oss.andrew.chooseandperish;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void resolveUrl(View view) {
        final TextView textDisplay = (TextView) findViewById(R.id.txtDisplay);
        EditText urlEdit = (EditText) findViewById(R.id.vanityUrlEdit);
        String vanity = urlEdit.getText().toString();
        String url = "https://api.steampowered.com/ISteamUser/ResolveVanityURL/v1/"
                + "?key=removed"
                + "&vanityurl=" + vanity
                + "&url_type=1";

        getSteamResponse(url, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                String steamid = result.optString("steamid");
                textDisplay.setText(steamid);
            }
        });
    }

    /*public void getOwnedGames() {
        final TextView textDisplay = (TextView) findViewById(R.id.txtDisplay);
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
                            textDisplay.setText(gameCount);
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

        RequestSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
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
        RequestSingleton.getInstance(this).addToRequestQueue(jsObjRequest);

    }

    public interface VolleyCallback {
        void onSuccess(JSONObject result);
    }

}
