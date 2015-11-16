@echo off
@echo Version pusher invoked.
VersionPusherGUI.exe ..\..\SysComProcessor\assets\version.xml
copy /Y ..\..\SysComProcessor\assets\version.xml ..\..\GPSToggler\assets\version.xml
@echo Version pusher finished.
