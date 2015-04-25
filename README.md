Prism Lights
============

Prism Lights is an application that controls Philips Hue Lights through iPhone and Android. It allows the user to control the light bulbs state remotely, as well as interfacing with the bridge to set schedules and timers. Additionaly, it has some interesting features including the ability to set favorites, cause the light or group to cycle through a specific set of states, control the bulbs through music, voice, and bluetooth tracking. Our application does require a Philips Hue Lights bridge in order to work correctly.

iPhone Application-Build and Run
----------------------------------
* Clone the repository on github at https://github.com/AlmaKnudson/Senior-Project-Team-Prism.git onto your Mac
* Install Xcode 6.3
* Open the "iPhone/Prism Lights" folder in the repository
* Open the Prism Lights.xcodeproj
* To run on the simulator click the run button in the top left corner
* To run on your iPhone:
  + Sign up for a developer account on developer.apple.com and follow prompts to activate
  + Connect your iPhone via USB to your computer
  + Switch the device next to the run button to your connected device
  + Click the run button

Android Application-Build and Run
---------------------------------
* Clone the repository on github at https://github.com/AlmaKnudson/Senior-Project-Team-Prism.git
* Install Android Studio 1.1.0 and the Android SDK
* Open the Android SDK manager and install:
  + Android Support Library 22.1.1
  + Android Support Repository 14
  + Android SDK Tools 24.1.2 
  + Android SDK Platform Tools 22
  + Android SDK Build-tools 21.1.2
  + Android SDK Platform API 21 
  + Android Google APIs API 21
* To run on the simulator:
  + Open the Android SDK Manager and install one of the System Images for API 21
  + Open the Android Virtual Device Manager and create a virtual device using the system image previously downloaded
  + Click Run or Debug
  + Choose to start the emulator
  + Unlock the emulated device
* To run on your Android Phone(Your phone's API must be greater than 19 although 21 is preferred):
  + Insure that you have debugging mode enabled ([How do I do that?](http://www.kingoapp.com/root-tutorials/how-to-enable-usb-debugging-mode-on-android.htm))
  + Connect your device to your computer via USB
  + Allow debugging from your computer in the dialog that pops up on your phone
  + Click Run or Debug in Android Studio
  + Choose to run on your device
