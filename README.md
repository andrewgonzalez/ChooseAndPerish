# ChooseAndPerish

Copyright (c) 2016 Andrew Gonzalez
andrew.gonzal@gmail.com

An android app that chooses a random game for you to play from your steam account.
This project uses Google's Volley framework for transmitting the requests to the
Steam web api.
The volley code can be found here: (https://android.googlesource.com/platform/frameworks/volley)

### How It Works:
Enter the vanity url for your steam profile and press Play! A random game from a list
of games you own will appear and YOU MUST PLAY IT. The RNGods will be upset if you don't.

Got friends? Enter their vanity urls as well (up to four players) and a game all of you own
will be chosen.

### Requirements:
Two things are required for each steam profile for this to work:
+ The players' steam profile be public (the api will only return a list of owned games
from a public profile)
+ A vanity url is set in each player's profile

This code is available under the "MIT License". See the COPYING file in this distribution for license terms.
