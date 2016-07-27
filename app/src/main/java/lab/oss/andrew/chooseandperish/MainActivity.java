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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private final int INITIAL_CAPACITY = 200;
    private Map<String, Integer> mGameMap;
    private int mMapDone;
    private int mPlayerCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGameMap = new HashMap<String, Integer>(INITIAL_CAPACITY);

    }

    public void startWork(View view) {
        mGameMap.clear();
        mPlayerCount = 0;
        mMapDone = 0;

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainLayout);
        EditText urlEdit;

        for (int i = 0; i < layout.getChildCount(); i++) {
            View v = layout.getChildAt(i);
            if (v instanceof EditText) {
                urlEdit = (EditText) v;
                if (!urlEdit.getText().toString().trim().equals("")) {
                    mPlayerCount++;
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
                if (mPlayerCount > 1) {
                    // call method to map all players' games to a HashMap
                    mapGames(result.optJSONArray("games"));
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

    public void mapGames(JSONArray games) {
        // For each entry in the array (for each game), check if the JSON object is in the map.
        // If it is, increment the value associated with that game.
        // The value stored with the JSON object reflects how many players own that game.
        String key;
        Integer value;
        JSONObject gameObject;
        for (int i = 0; i < games.length(); i++) {
            gameObject = games.optJSONObject(i);
            key = gameObject.optString("name");
            value = mGameMap.get(key);
            if (value == null) {
                mGameMap.put(key, 1);
            } else {
                mGameMap.put(key, value + 1);
            }
        }
        mMapDone++;

        // This is probably a terrible way of tracking when the multiple calls to mapGames are
        // finished.
        if (mMapDone == mPlayerCount) {
            ArrayList<String> ownedByAll = new ArrayList<String>();
            for (Map.Entry<String, Integer> entry : mGameMap.entrySet()) {
                if (entry.getValue() == mPlayerCount) {
                    ownedByAll.add(entry.getKey());
                }
            }

            // At this point I should have a JSONArray object of only games that are owned
            // by all players.
            randomGame(ownedByAll);
        }
    }

    public void randomGame(JSONArray gameList) {
        Random random = new Random();
        int randNum = random.nextInt(gameList.length());

        JSONObject myGame = gameList.optJSONObject(randNum);

        TextView txtDisplay = (TextView) findViewById(R.id.txtDisplay);
        txtDisplay.setText(myGame.optString("name"));
    }

    public void randomGame(ArrayList<String> gameList) {
        Random random = new Random();
        int randNum = random.nextInt(gameList.size());

        TextView txtDisplay = (TextView) findViewById(R.id.txtDisplay);
        txtDisplay.setText(gameList.get(randNum));
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

    public interface VolleyCallback {
        void onSuccess(JSONObject result);
        void onFail(VolleyError error);
    }

}
