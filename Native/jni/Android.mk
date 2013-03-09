LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    		:= operator
LOCAL_SRC_FILES 		:= main.cpp
LOCAL_LDLIBS    		:= -llog
LOCAL_FORCE_STATIC_EXECUTABLE 	:= true
LOCAL_STATIC_LIBRARIES 		:= libc log

include $(BUILD_EXECUTABLE)


