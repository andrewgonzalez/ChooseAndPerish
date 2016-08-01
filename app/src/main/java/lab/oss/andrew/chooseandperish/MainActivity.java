/*
 * Copyright (c) 2016 Andrew Gonzalez
 * This code is available under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */

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
    private Map<Integer, Integer> mGameMap;
    private int mMapDone;
    private int mPlayerCount;
    private int mAppCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGameMap = new HashMap<Integer, Integer>(INITIAL_CAPACITY);

    }

    public void startWork(View view) {
        mGameMap.clear();
        mPlayerCount = 0;
        mAppCount = 0;
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

    private void resolveUrl(final String vanityUrl) {
        String url = "https://api.steampowered.com/ISteamUser/ResolveVanityURL/v1/"
                + "?key=removed"
                + "&vanityurl=" + vanityUrl
                + "&url_type=1";

        getSteamResponse(url, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                JSONObject response = result.optJSONObject("response");
                try {
                    if (response.getInt("success") == 1) {
                        String steamID = response.optString("steamid");
                        getOwnedGames(steamID);
                    } else {
                        informUser("Unsuccessful response for " + vanityUrl
                                + ". Could be a typo or their profile is not public.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFail(VolleyError error) {
                error.printStackTrace();
            }
        });

    }

    private void getOwnedGames(String steamID) {
        String url = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/"
                + "?key=removed"
                + "&steamid=" + steamID
                + "&include_appinfo=1"
                + "&format=json";

        getSteamResponse(url, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    JSONObject response = result.getJSONObject("response");
                    if (mPlayerCount > 1) {
                        // call method to map all players' games to a HashMap
                        mapGames(response.getJSONArray("games"));
                    } else {
                        // there is only 1 player, just choose a random game
                        randomGame(response.getJSONArray("games"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFail(VolleyError error) {
                error.printStackTrace();
            }
        });
    }

    private void mapGames(JSONArray games) {
        // For each entry in the array (for each game), check if the JSON object is in the map.
        // If it is, increment the value associated with that game.
        // The value stored with the JSON object reflects how many players own that game.
        Integer key;
        Integer value;
        JSONObject gameObject;
        for (int i = 0; i < games.length(); i++) {
            try {
                gameObject = games.getJSONObject(i);
                key = gameObject.getInt("appid");
                value = mGameMap.get(key);
                if (value == null) {
                    mGameMap.put(key, 1);
                } else {
                    mGameMap.put(key, value + 1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        mMapDone++;

        // This is probably a terrible way of tracking when the multiple calls to mapGames are
        // finished.
        if (mMapDone == mPlayerCount) {
            ArrayList<Integer> ownedByAll = new ArrayList<Integer>();
            for (Map.Entry<Integer, Integer> entry : mGameMap.entrySet()) {
                if (entry.getValue() == mPlayerCount) {
                    ownedByAll.add(entry.getKey());
                }
            }

            // At this point I should have a list of only games that are owned
            // by all players. It's possible that not all players will have games in common.
            if (ownedByAll.size() == 0) {
                informUser("Can this be? Not all players have a game in common!");
            } else {
                getAppInfo(ownedByAll);
            }
        }
    }

    // Fetches information about a game from the steam store.
    // I'm looking for the categories to find games that have the multi-player
    // and co-op tags.
    private void getAppInfo(final ArrayList<Integer> appIDList) {
        final ArrayList<String> multiplayerGames = new ArrayList<String>();

        for (final Integer appID : appIDList) {
            String url = "http://store.steampowered.com/api/appdetails"
                    + "?appids=" + appID.toString();

            getSteamResponse(url, new VolleyCallback() {
                @Override
                public void onSuccess(JSONObject result) {
                    JSONObject appInfo = result.optJSONObject(appID.toString());
                    try {
                        // The json response will have key:value pair indicating success as true
                        // or false. It's possible that a store listing for an appid doesn't
                        // exist, but both players may still own the game.
                        if (appInfo.getBoolean("success")) {
                            JSONObject data = appInfo.getJSONObject("data");
                            String name = data.getString("name");
                            JSONArray categories = data.getJSONArray("categories");
                            for (int i = 0; i < categories.length(); i++) {
                                // Category IDs 1 and 9 are Multi-player and Co-op respectively.
                                // This loop will grab only games that have those IDs.
                                JSONObject category = categories.getJSONObject(i);
                                if (category.getInt("id") == 1 || category.getInt("id") == 9) {
                                    multiplayerGames.add(name);
                                    // Break out of the loop so the game is added multiple times
                                    // for having both categories.
                                    break;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mAppCount++;
                    if (mAppCount == appIDList.size()) {
                        // All the multiplayer/co-op games have been added to multiplayerGames
                        // list. Send it off for random game selection. It is possible that every
                        // player won't have a multiplayer game in common.
                        if (multiplayerGames.size() != 0) {
                            randomGame(multiplayerGames);
                        } else {
                            informUser("You have games in common, but none of them are multiplayer!");
                        }
                    }
                }

                @Override
                public void onFail(VolleyError error) {
                    error.printStackTrace();
                }
            });
        }
    }

    private void randomGame(JSONArray gameList) {
        Random random = new Random();
        int randNum = random.nextInt(gameList.length());

        JSONObject myGame = null;
        try {
            myGame = gameList.getJSONObject(randNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (myGame != null) {
            informUser(myGame.optString("name"));
        } else {
            informUser("Encountered a null element during random game selection.");
        }
    }

    private void randomGame(ArrayList<String> gameList) {
        Random random = new Random();
        int randNum = random.nextInt(gameList.size());

        informUser(gameList.get(randNum));
    }

    private void informUser(String message) {
        TextView txtDisplay = (TextView) findViewById(R.id.txtDisplay);
        txtDisplay.setText(message);
    }

    private void getSteamResponse(String url, final VolleyCallback callback) {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Do something
                        callback.onFail(error);
                    }
                });
        VolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);

    }

    public interface VolleyCallback {
        void onSuccess(JSONObject result);
        void onFail(VolleyError error);
    }

}
