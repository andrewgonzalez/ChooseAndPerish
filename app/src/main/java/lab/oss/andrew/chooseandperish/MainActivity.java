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
import android.widget.ProgressBar;
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

    private ProgressBar mProgressBar;
    private final int INITIAL_CAPACITY = 200;
    private final String key = ApiKey.API_KEY;
    private Map<Integer, Integer> mGameMap;
    private int mMapDone;
    private int mPlayerCount;
    private int mAppCount;

    // Variables for saving the app state
    static final String MAP_DONE = "mapDone";
    static final String PLAYER_COUNT = "playerCount";
    static final String APP_COUNT = "appCount";
    static final String TXT_DISPLAY = "txtDisplay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mGameMap = new HashMap<Integer, Integer>(INITIAL_CAPACITY);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore saved state members
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current state
        savedInstanceState.putInt(MAP_DONE, mMapDone);
        savedInstanceState.putInt(PLAYER_COUNT, mPlayerCount);
        savedInstanceState.putInt(APP_COUNT, mAppCount);
        savedInstanceState.putString(TXT_DISPLAY, findViewById(R.id.txtDisplay).toString());

        super.onSaveInstanceState(savedInstanceState);
    }

    public void startWork(View view) {
        mGameMap.clear();
        mPlayerCount = 0;
        mAppCount = 0;
        mMapDone = 0;

        informUser(getString(R.string.start_work));
        mProgressBar.setVisibility(View.VISIBLE);

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
                + "?key=" + key
                + "&vanityurl=" + vanityUrl
                + "&url_type=1";

        getSteamResponse(url, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                JSONObject response = result.optJSONObject("response");
                try {
                    if (response.getInt("success") == 1) {
                        String progressText = String.format(getString(R.string.resolve_url_success), vanityUrl);
                        informUser(progressText);
                        String steamID = response.getString("steamid");
                        getOwnedGames(steamID);
                    } else {
                        String progressText = String.format(getString(R.string.resolve_url_fail), vanityUrl);
                        informUser(progressText, true);
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
                + "?key=" + key
                + "&steamid=" + steamID
                + "&include_appinfo=1"
                + "&format=json";

        informUser(getString(R.string.get_games));
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

        informUser(getString(R.string.mapping_games));

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
                informUser(getString(R.string.no_common_games), true);
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

        informUser(getString(R.string.find_multiplayer));

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
                            informUser(getString(R.string.no_multiplayer), true);
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

        mProgressBar.setVisibility(View.INVISIBLE);

        JSONObject myGame = null;
        try {
            myGame = gameList.getJSONObject(randNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (myGame != null) {
            informUser(myGame.optString("name"));
        } else {
            informUser(getString(R.string.random_null), true);
        }
    }

    private void randomGame(ArrayList<String> gameList) {
        Random random = new Random();
        int randNum = random.nextInt(gameList.size());

        mProgressBar.setVisibility(View.INVISIBLE);
        informUser(gameList.get(randNum));
    }

    private void informUser(String message) {
        TextView txtDisplay = (TextView) findViewById(R.id.txtDisplay);
        txtDisplay.setText(message);
    }

    private void informUser(String message, boolean error) {
        TextView txtDisplay = (TextView) findViewById(R.id.txtDisplay);
        txtDisplay.setText(message);

        if (error) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
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
