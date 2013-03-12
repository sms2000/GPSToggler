THIS CODE IS 'UNRESTRICTED PUBLIC DOMAIN' LICENCED.


GPSToggler
==========

GPS toggle widget for Android rooted devices. It wrks well even for those ROMs and kernels, other software failed.
It requires the 'su' privilegies only during the very first run (reboot is still required). 


Versions:

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

