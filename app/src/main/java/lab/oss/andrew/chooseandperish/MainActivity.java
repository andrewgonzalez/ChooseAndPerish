package lab.oss.andrew.chooseandperish;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private JSONObject mJsonResponse = new JSONObject();
    private JSONArray mJsonArray = new JSONArray();
    private int playerCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void startWork(View view) {
        playerCount = 0;
        ArrayList<String> playerUrls = new ArrayList<String>();
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainLayout);
        EditText urlEdit;

        for (int i = 0; i < layout.getChildCount(); i++) {
            View v = layout.getChildAt(i);
            if (v instanceof EditText) {
                urlEdit = (EditText) v;
                if (!urlEdit.getText().toString().trim().equals("")) {
                    playerCount++;
                    resolveUrl(urlEdit.getText().toString());
                }
            }
        }



    }

    public void resolveUrl(String vanityUrl) {
        String url = "https://api.steampowered.com/ISteamUser/ResolveVanityURL/v1/"
                + "?key=removed"
                + "&vanityurl=" + vanityUrl
                + "&url_type=1";

        getSteamResponse(url, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                String steamID = result.optString("steamid");
                getOwnedGames(steamID);
            }
            @Override
            public void onFail(VolleyError e) {
                e.printStackTrace();
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
                //mJsonArray = result.optJSONArray("games");
                if (playerCount > 1) {
                    // call method to map all players' games to a HashMap
                } else {
                    // there is only 1 player, just choose a random game
                    randomGame(result.optJSONArray("games"));
                }
            }
            @Override
            public void onFail(VolleyError e) {
                e.printStackTrace();
            }
        });
    }

    public void randomGame(JSONArray gameList) {
        Random random = new Random();
        int randNum = random.nextInt(gameList.length());

        JSONObject myGame = gameList.optJSONObject(randNum);

        TextView txtDisplay = (TextView) findViewById(R.id.txtDisplay);
        txtDisplay.setText(myGame.optString("name"));
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
                        callback.onFail(e);
                    }
                });
        VolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);

    }

    private void setmJsonResponse(JSONObject object) {
        this.mJsonResponse = object;
    }

    public interface VolleyCallback {
        void onSuccess(JSONObject result);
        void onFail(VolleyError error);
    }

}
