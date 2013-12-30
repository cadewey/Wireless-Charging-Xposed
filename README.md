##Wireless Charging Xposed

Android 4.2.2 added a notification sound that plays whenever a device is placed on a wireless charger, 
but provided no way to disable it. Wireless Charging Xposed is a module for the Xposed Framework that 
aims to add a bit more flexibility in how your phone notifies you when you begin wireless charging.

##How it works

The commit to AOSP that added the notification sound can be found [here](https://android.googlesource.com/platform/frameworks/base/+/84e2756c0f3794c6efe5568a9d09101ba689fb39%5E!/). Essentially,
this module hooks into the `playWirelessChargingStartedSound()` method and does one (or more) of several 
things, depending on the user's settings (though not in this order):

 1. Returns without doing anything at all (complete silence)
 2. Performs 2 short vibrations in quick succession
 3. Invokes the original method, playing the default notification sound as normal OR
 4. Plays a custom-selected notification sound
 
The exact behavior is left up to the user, and can be any combination of 1,2 and 3 OR 4 above.

##Building the module

Wireless Charging Xposed was built in Eclipse, and doesn't contain the necessary project definitions
for Android Studio currently. They may be added at a later date, though there are no immediate plans
to support Android Studio, at least not until it is "stable".

Along with Eclipse, you'll need the standard ADT plugin and the Android 4.2.2 platform framework/APIs.

With that done, you'll then need two things in order to build this module:

 1. This repository; clone it somewhere and import it into your Eclipse workspace.
 2. A copy of the [XposedLibrary](https://github.com/rovo89/XposedMods/tree/master/XposedLibrary) codebase. Again clone this and import it into your Eclipse workspace.
    Make sure it is set to build as a library, and make the module's project reference it.

With those two sets of code set up, you should be able to build, deploy, and debug via Eclipse.

##Binary downloads and support

Binaries will not be hosted on GitHub, but can be obtained from other locations instead:

 1. The associated [XDA thread](http://forum.xda-developers.com/showthread.php?t=2587431)
 2. The [Xposed Module Repository](http://repo.xposed.info/module/com.eldarerathis.xposedmodule.wirelesschargingxposed) (also accessible via the Xposed Installer on your device)
 3. [Google Play](https://play.google.com/store/apps/details?id=com.eldarerathis.xposedmodule.wirelesschargingxposed) - this version is $0.99 USD (a "donate" version). It **does not** have advertising.
 
##License

Wireless Charging Xposed is released under version 3 of the GNU General Public License (GPLv3).
 
