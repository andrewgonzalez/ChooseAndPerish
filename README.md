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

When entering your url, just enter the custom text you set in your profile, not the
entire http://steamcommunity.com/id/custom url, just the custom url part.

### Requirements:
Two things are required for each steam profile for this to work:
+ The players' steam profile be public (the api will only return a list of owned games
from a public profile)
+ A vanity url is set in each player's profile

### What is a vanity url?
A vanity url, also known as a custom url, is... well just as it says a custom url you can
enter to make navigating or linking to your steam profile easier. You can set it to whatever
you like (so long as it isn't taken) from your steam profile.

To set it, log in to steam and navigate to Edit Profile. There you should see a
listing for 'Custom URL' with a textbox. Enter whatever you like!

This code is available under the "MIT License". See the COPYING file in this distribution for license terms.
