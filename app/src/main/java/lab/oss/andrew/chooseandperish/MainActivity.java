package lab.oss.andrew.chooseandperish;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private JSONObject mJsonResponse = new JSONObject();
    private JSONArray mJsonArray = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void startWork(View view) {
        ArrayList<String> playerUrls = new ArrayList<String>();
        EditText urlEdit;

        for (int i = 0; i < 4; i++) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainLayout);
            View v = layout.getChildAt(i);
            if (v instanceof EditText) {
                urlEdit = (EditText) v;
                if (urlEdit.getText().toString().equals("")) {
                    playerUrls.add(urlEdit.getText().toString());
                }
            }
        }

        //String steamid = resolveUrl(playerUrls);

    }

    public void resolveUrl(View view) {
        EditText urlEdit = (EditText) findViewById(R.id.playerOne);
        String vanity = urlEdit.getText().toString();
        String url = "https://api.steampowered.com/ISteamUser/ResolveVanityURL/v1/"
                + "?key=removed"
                + "&vanityurl=" + vanity
                + "&url_type=1";

        getSteamResponse(url, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                String steamID = result.optString("steamid");
                getOwnedGames(steamID);
            }
        });

    }

    public void getOwnedGames(String steamID) {
        String url = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/"
                + "?key=removed"
                + "&steamid=" + steamID
                + "&include_appinfo=1"
                + "&format=json";

        getSteamResponse(url, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                mJsonArray = result.optJSONArray("games");
            }
        });
    }

    private void getSteamResponse(String url, final VolleyCallback callback) {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            callback.onSuccess(response.getJSONObject("response"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        // Do something
                        e.printStackTrace();
                    }
                });
        VolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);

    }

    private void setmJsonResponse(JSONObject object) {
        this.mJsonResponse = object;
    }

    public interface VolleyCallback {
        void onSuccess(JSONObject result);
    }

}
