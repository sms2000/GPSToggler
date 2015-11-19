@echo off
@echo GPSToggler version update.
xcopy ..\Prebuilds\BatchPRocessor\version.xml assets\version.xml /Y
@echo GPSToggler version updated.

@echo GPSToggler copy stub.

copy /Y ..\SysComProcessor\bin\SysComProcessor.apk 	libs\armeabi\libscp.so
copy /Y ..\SysComProcessor\bin\SysComProcessor.apk 	libs\armeabi-v7a\libscp.so
copy /Y ..\SysComProcessor\bin\SysComProcessor.apk 	libs\arm64-v8a\libscp.so

copy /Y ..\SysComProcessor\bin\SysComProcessor.apk 	libs\x86\libscp.so
copy /Y ..\SysComProcessor\bin\SysComProcessor.apk 	libs\x86_64\libscp.so

copy /Y ..\SysComProcessor\bin\SysComProcessor.apk 	libs\mips\libscp.so
copy /Y ..\SysComProcessor\bin\SysComProcessor.apk 	libs\mips64\libscp.so

@echo GPSToggler stub copied.
