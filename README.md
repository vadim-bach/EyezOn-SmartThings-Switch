# EyezOn SmartThings Switch
This assumes you have:
* [SmartThings Hub](https://www.smartthings.com/products/smartthings-hub)
* Home security system coupled to an [EyezOn EnvisaLink](http://www.eyezon.com/index.php) module  (tested only with EnvisaLink v4)

This program will enable you to create a virtual switch in your SmartThings that will integrate with your EnvisaLink module. This will allow you to arm/disarm your home security system in the desired mode (stay/away) through SmartThings, just like with any other switch. Then you can do cool stuff like:
* [Away] Arm system when everyone leaves the house
* Disarm system when someone arrives
* [Stay] Arm system by saying "Alexa, Goodnight!" (via SmartThings-Alexa integration)
* Disarm system by saying "Alexa, Good Morning!" (via SmartThings-Alexa integration)

**Note: This program makes use of internet connection to EyezOn server and therefore requires your hub to be able to communicate with the outside world i.e. it is NOT executed locally and will not work when your home internet connection is down.**

# Installation Steps

1. Login to your [SmartThings account](https://account.smartthings.com/login)
1. Create a new Device Handler **From Code**
1. Copy the code from the correcct **eyezon-smartthings-switch-xxxxxxx.groovy** file (based on your version of EnvisaLink) into the text field and hit **Create**
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
    * Account ID: The Account ID you've written down above
    * Device ID: The Device ID you've written down above
    * Partition #: Partition that you'd like to control with this switch (generally "1" unless you have more than one partition)
    * Disarm PIN: PIN/password you use to disarm the system. This will effectively be stored inside your hub and SmartThings cloud account.
    * Arm Mode: Select the correct arm mode for your switch ("Away" for the "Eyez-On Away" switch, and "Stay" for the "Eyez-On Stay" switch)
1. Add the switches to your SmartThings Routines as desired. For example, you can update your **Goodbye!** routine to turn **on** the **Eyez-On Away** switch (make sure to configure the routine to run automatically when **Everyone Leaves**). Similarly, you can amend your **I'm Back!** routine to turn **off** the **Eyez-On Away** switch (and automate it to run when **Someone Arrives**).
1. You can go one step further and add automation in Alexa (assuming it's already integrated with your SmartThings App) to trigger arming/disarming the switches on command (probably only makes sense with the **Eyez-On Stay** switch)

**Note: The manual toggling of the switches in SmartThings app does not work well due to the lack of status updating/polling functionality. Please refrain from toggling the switches manually in the App. (I'm hopeful I'll get some free time to get it fixed at some point, if someone else doesn't)**
