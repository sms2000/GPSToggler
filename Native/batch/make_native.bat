@echo Native code maker begin
call ndk-build
ren ..\libs\x86\operator 			liboperator.so
ren ..\libs\mips\operator			liboperator.so
ren ..\libs\armeabi\operator 		liboperator.so
ren ..\libs\armeabi-v7a\operator 	liboperator.so
@echo Native code maker finished


