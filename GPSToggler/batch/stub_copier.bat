@echo off
@echo SysComProcessor copier.
copy ..\..\SysComProcessor\bin\SysComProcessor.apk ..\libs\armeabi\libscp.so /Y
copy ..\..\SysComProcessor\bin\SysComProcessor.apk ..\libs\armeabi-v7a\libscp.so /Y
copy ..\..\SysComProcessor\bin\SysComProcessor.apk ..\libs\x86\libscp.so /Y
copy ..\..\SysComProcessor\bin\SysComProcessor.apk ..\libs\mips\libscp.so /Y
@echo SysComProcessor copier finished.
