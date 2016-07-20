package lab.oss.andrew.chooseandperish;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        final TextView textDisplay = (TextView) findViewById(R.id.txtDisplay);
        String url = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=2E2F2BEA53C917775B3AEA7548391CC3&steamid=76561197992593866&include_appinfo=1&format=json";


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
    }
}
