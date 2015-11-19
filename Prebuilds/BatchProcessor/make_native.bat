@echo Native code maker begin
call ndk-build
@echo Native code maker finished

@echo Native copier.
mkdir ..\GPSToggler\libs

mkdir ..\GPSToggler\libs\armeabi
mkdir ..\GPSToggler\libs\armeabi-v7a
mkdir ..\GPSToggler\libs\arm64-v8a
mkdir ..\GPSToggler\libs\x86
mkdir ..\GPSToggler\libs\x86_64
mkdir ..\GPSToggler\libs\mips
mkdir ..\GPSToggler\libs\mips64

copy /Y libs\armeabi\operator  		..\GPSToggler\libs\armeabi\liboperator.so
copy /Y libs\armeabi-v7a\operator  	..\GPSToggler\libs\armeabi-v7a\liboperator.so
copy /Y libs\arm64-v8a\operator  	..\GPSToggler\libs\arm64-v8a\liboperator.so
copy /Y libs\x86\operator		..\GPSToggler\libs\x86\liboperator.so
copy /Y libs\x86_64\operator		..\GPSToggler\libs\x86_64\liboperator.so
copy /Y libs\mips\operator  		..\GPSToggler\libs\mips\liboperator.so
copy /Y libs\mips64\operator  		..\GPSToggler\libs\mips64\liboperator.so
@echo Native finished.

