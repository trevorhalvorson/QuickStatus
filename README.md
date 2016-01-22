# Quick Status for Android Wear


Quick Status lets you post to Facebook using your Android Wear device.


![ScreenShot](/screenshots/web_icon.png)


## Setup


1. Rename all `com.trevorhalvorson.quickstatus` packages to your own unique package name.

    See [Stack Overflow](http://stackoverflow.com/questions/16804093/android-studio-rename-package)
    for various ways to do this.

2. Visit [Facebook for Developers](https://developers.facebook.com/),
    create a new application for Android, and follow the directions to find your App ID.
    If you have an issue generating a key hash, see this [SO post](http://stackoverflow.com/a/13488560/5036517).

3. Finally, add your Facebook App ID in `strings.xml`:

    `<string name="facebook_app_id">YOUR_APP_ID</string>`


## Going Live


Applications requesting the `publish_actions` permission from Facebook users are required to have their
app reviewed by Facebook before it can be published for download. More info [here](https://developers.facebook.com/docs/facebook-login/review/faqs#what_is_review).


## Screenshots


![ScreenShot](/screenshots/wear_square_screenshot.png)


![ScreenShot](/screenshots/login_screenshot.png)


![ScreenShot](/screenshots/main_screenshot.png)