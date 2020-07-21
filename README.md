# EyezOn SmartThings Switch
This assumes you have:
* [SmartThings Hub](https://www.smartthings.com/products/smartthings-hub)
* Home security system coupled to an [EyezOn EnvisaLink](http://www.eyezon.com/index.php) module  (tested with versions of EnvisaLink 3 and 4)

This program will enable you to create a virtual switch in your SmartThings that will integrate with your EnvisaLink module. This will allow you to arm/disarm your home security system in the desired mode (stay/away) through SmartThings, just like with any other switch. Then you can do cool stuff like:
* [Away] Arm system when everyone leaves the house
* Disarm system when someone arrives
* [Stay] Arm system by saying "Alexa, Goodnight!" (via SmartThings-Alexa integration)
* Disarm system by saying "Alexa, Good Morning!" (via SmartThings-Alexa integration)

**Note: This program makes use of internet connection to EyezOn server and therefore requires your hub to be able to communicate with the outside world i.e. it is NOT executed locally and will not work when your home internet connection is down.**

# Installation Steps

1. Login to your [SmartThings account](https://account.smartthings.com/login)
1. Create a new Device Handler **From Code**
1. Copy the code from **eyezon-smartthings-switch-variant-xx.groovy** file into the text field and hit **Create**. See important note<sup>1</sup> about which variant to choose below.
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
1. Login to your [Eyez-On Portal](https://www.eyez-on.com/EZMAIN/login.php)
1. On the left hand side you should see "My House" (or however you named your house) together with an alpha-numeric set of characters of the form "002E1F3831NB". This is your **Device ID** - write it down!
1. Go to **Account Settings** and under **Generate Mobile Portal Link** hit **Generate New Link** button (if you haven't already). You should see a link of the form: https://www.eyez-on.com/EZMOBILE/index.php?mid=039aac8304eff039388ed9a3029cdde. The long alpha-numeric sequence that follows "mid=" is your **Account ID** - write it down!
1. Open SmartThings app on your phone. You should now see the two switches "Eyez-On Away" and "Eyez-On Stay" you created above
1. For each switch, open its configuration and fill in all required values:
    * Account ID: The Account ID you've written down above (case sensitive!)
    * Device ID: The Device ID you've written down above (case sensitive!)
    * Partition #: Partition that you'd like to control with this switch (generally "1" unless you have more than one partition)
    * Partition Label: Label of the partition as it appears in your EyezOn app (generally "Partition 1" unless you customized it)
    * Disarm PIN: PIN/password you use to disarm the system. This will effectively be stored inside your hub and SmartThings cloud account. (known issue around PINs that begin with '0')
    * Arm Mode: Select the correct arm mode for your switch ("Away" for the "Eyez-On Away" switch, and "Stay" for the "Eyez-On Stay" switch)
    * Exit Delay: Set the exit delay to match the one configured with your alarm. Enter number as integer e.g. "60" as opposed to "60.0". (Note: this is only being used to tell the handler when to check/refresh system status after arm/disarm).
1. Add the switches to your SmartThings Routines as desired. For example, you can update your **Goodbye!** routine to turn **on** the **Eyez-On Away** switch (make sure to configure the routine to run automatically when **Everyone Leaves**). Similarly, you can amend your **I'm Back!** routine to turn **off** the **Eyez-On Away** switch (and automate it to run when **Someone Arrives**).
1. You can go one step further and add automation in Alexa (assuming it's already integrated with your SmartThings App) to trigger arming/disarming the switches on command (probably only makes sense with the **Eyez-On Stay** switch)

# Notes:  
**(1)** There are two variants of the code; it seems that, based on some odd combination of hardware and/or software variant-01 works for some people, and variant-02 for others. You can determine which variant to use based on the format of the request that EyezOn website makes to the server (if you know how to use browser dev tools to look that up):
  * Variant 01: if on system arm request body contains param 'hextaction'
  * Variant 02: if on system arm request body does NOT contain param 'hextaction'
  
Otherwise, just try one - if it doesn't work, try the other one.

# Contributors:
* [John Constantelos](https://github.com/jsconstantelos)

