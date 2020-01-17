# GpxCompare
GpxCompare is a Java application similar to Strava flyby's. You can upload any 2 GPX tracks and compare the time difference for all matching points.

# Information
For this project 3 different projects have been used:
1. [JPX](https://github.com/jenetics/jpx)
2. [JXMapViewer2](https://github.com/msteiger/jxmapviewer2)
3. [compare_gpx](https://github.com/jonblack/cmpgpx)

JPX has been modified very slightly, I added the option to set the name of a GPX object. This so that I can show the user which files they have added.

## Installation
Not much to install, there is a zip with a jar file. If you want to run this application, unpack the zip and run 'run.bat'. [The zip-file can be found here](GPSCompare.rar)

## ToDo
* Recalculate the GPX tracks upon editing equalization/gap penalty instead of deleting them
* More than 2 tracks at the same time
* Calculate the time difference as if everybody had started at the same time
* As above, replay the race(The crosshairs) as if everybody had started at the same time
* Cleanup code
* Show the graph and the map in one screen
* Automatically set different colors for different
* Actually show some properties in the properties window
