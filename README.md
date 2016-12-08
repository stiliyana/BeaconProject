# BeaconProject

Android app that detects beacons and locates the user based on distance between beacons and device. 

## Progress:

### Done:
- Full support of latest android SDK 25 and build tools;
- Handling permissions from Android Marshmallow and above;
- Beacons stored in Shared Preferences for offline viewing including latest discovery time.
- Bluetooth manual search;
- Usage of AltLibrary for parsing Raw bytes data to get useful information from beacons (name, address, discovery time, distance, etc.);
- Distance calculation using CurveFittedAlgorithm;

### To do:
- Fingerprinting locations and location calculation for indoor navigation;
- Improve GUI

