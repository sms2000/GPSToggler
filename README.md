=======
THIS CODE IS 'UNRESTRICTED PUBLIC DOMAIN' LICENCED.


GPSToggler
==========

GPS toggle widget for Android rooted devices. It wrks well even for those ROMs and kernels, other software failed.
It requires the 'su' privilegies only during the very first run (reboot is still required). 


Versions:

1.0.511
-------

1. 1st practially used release


0.54
----

1. You may select now which applications to look for when automatically enabling/disablonh GPS. Yet some bugs are expected.

2. Minor improvements.


0.42
----

1. KitKat 4.4 compatible (verified with Note 2 OmniRom AOSP 4.4.2).

2. Native code reboot implemented as should be.

3. Interface improvements: four types of the icon instead of three. 



0.40
----

Fixed (apparently) the known problem with Galaxy Note 2, S3, etc 4.3 ROMs where SU is not well processed. 
The hotfix is explained in detail in the SU source code.


0.19
----

The log is simplified.
THe main service is self-repairing after Android killed it.
Optional notification and icon added.


0.16
----

Improved (up to some level) the recognition of running GPS software and especially the finishing of the run.
Little log improvement for above process.


0.15
----

Added two passive activities: 'OnActivity' and 'OffActivity'.
Those two used to automate GPS on/off from 3rd party applications.

Added 'GPSActivityTest' application as an example of above.


0.14
----

Implemented fully automatic uninstall for the system module. 

'SU' utilization is minimized. The application asks for the 'SU' permission only once during the first run.

Implemented the 'SU' bypass algorithm (own local 'SU') to minimize the user bothering. 


0.13
----

Implemented semi-automatic uninstall for the system module.


Before 0.13
-----------

Early public releases.

>>>>>>> refs/heads/master
