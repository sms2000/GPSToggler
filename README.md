==========
THIS CODE IS 'UNRESTRICTED PUBLIC DOMAIN UNGER GPLL' LICENCED except 3rd party code of 'AndroidProcesses' library with Apache license.

Let me know if you consider your rights violated.

* GPLL is the 'General Pet Lovers Licence' - the non-restricting license promoting the self control for humans.
If you a pet lover, pet adopter, veterinary personel or in any other way helping cats, dogs and other pets to survice you are free to use this code.
If you are a per hater or harmed any pet in any way you are also free to use this code yet you have to bear shame on you forever. *



GPSToggler
==========

GPS toggle widget for Android rooted devices. It works even for those ROMs and kernels, other software failed.
It requires the 'su' privilegies only during the very first run (reboot is still required). 

Now compatible with Android Marshmallow.

Utilizes parts of 'AndroidProcesses' library code courtesy Jared Rummler from
https://github.com/jaredrummler/AndroidProcesses


ANTIVIRUS REPORTS MALICIOUS SOFTWARE
====================================

1. AVG Antivirus for Android reports the GPSToggler can steal your SMS. 
   Actually this software has nothing to do with SMS at all (see the sources).


Versions:

2.4.603
-------

1. Android 5.1.1 - 6.0.1 support is mostly correct. 
   Verified with AOSP 6.0.1 and CM13.


2.2.578
-------

1. Various improvements (just don't remember what I did actually).


2.2.566
-------

1. GUI improvements (Notification bar icons).


2.2.564
-------

1. Compatible with 5.0.x, 5.1.x, 6.0.x Androids.

2. GUI improvements, bugfixes, etc.. 


1.4.553
-------

1. Projects structure improved.


1.4.538
-------

1. Bug fixes.

2. Batch processing settled.


1.3.527
-------

1. Bug fixes.


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
