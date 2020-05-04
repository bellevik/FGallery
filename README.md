# FGallery

## Install and run
1. Clone this repo
2. Add a resource file with your Flickr API key here `app/src/main/res/values/flickr.xml`:
```
<resources>
  <string name="api_key"><!-- YOUR API KEY HERE --></string>
</resources>
```
3. Open the project in Android Studio and run emulator from there OR use `gradlew`;

## Next up / TODO
This repository contains limited functionality due to the time constraints for
this task of ~4 hours. Here's a few improvements that should be done if given
more time:
1. Show network status / disable search field if network connection is lost.
2. Separate concerns of the FlickrApi class. Right now, this class contains both
API integration, threading and some functionality that is related to UX (debouncing).
This should be separated.
3. Use JAX to retrieve Flickr response instead of manual object mapping to `FlickrImage[]`.
4. Implement fullscreen image loading on thumbnail click (stub code is there).
5. Implement tests.
