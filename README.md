# EyezOn SmartThings Switch
This assumes you have:
* [SmartThings Hub](https://www.smartthings.com/products/smartthings-hub)
* Home security system coupled to an [EyezOn EnvisaLink](http://www.eyezon.com/index.php) module

This program will enable you to create a virtual switch in your SmartThings that will integrate with your EnvisaLink module. This will allow you to arm/disarm your home security system in the desired mode (stay/away) through SmartThings, just like with any other switch. Then you can do cool stuff like:
* [Away] Arm system when everyone leaves the house
* Disarm system when someone arrives
* [Stay] Arm system by saying "Alexa, Goodnight!" (via SmartThings-Alexa integration)
* Disarm system by saying "Alexa, Good Morning!" (via SmartThings-Alexa integration)

**Note: This program makes use of internet connection to EyezOn server and therefore requires your hub to be able to communicate with the outside world i.e. it is NOT executed locally and will not work when your home internet connection is down.**

# Installation Steps

1. Login to your [SmartThings account](https://account.smartthings.com/login)
1. Create a new Device Handler **From Code**
1. Copy the code from **eyezon-smartthings-switch.groovy** file into the text field and hit **Create**
1. Hit **Publish** button to push the handler to your hub
1. Go to **My Devices** tab
1. Create the "Away" switch by hitting **New Device** button and filling out the fields as shown below:
    * Name: Eyez-On Away
    * Label: Eyez-On Away
    * Device Network Id: eyez-on_away
    * Type: Eyez-On SmartThings Switch
    * Version: Published
    * Hub: Home Hub (or whatever your hub's name is)
    * Group: Entrance (or whichever group/room has your alarm system)
1. Create the "Stay" switch by hitting **New Device** button and filling out the fields as shown below:
    * Name: Eyez-On Stay
    * Label: Eyez-On Stay
    * Device Network Id: eyez-on_stay
    * Type: Eyez-On SmartThings Switch
    * Version: Published
    * Hub: Home Hub (or whatever your hub's name is)
    * Group: Entrance (or whichever group/room has your alarm system)
1. Open SmartThings app on your phone. You should now see the two switches "Eyez-On Away" and "Eyez-On Stay" you created above
1. Login to your [Eyez-On Portal](https://www.eyez-on.com/EZMAIN/login.php)
1. On the left hand side you should see "My House" (or however you named your house) together with an alpha numeric set of characters of the form "002E1F3831NB". This is your **Device ID** - write it down!
