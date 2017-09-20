#!/bin/sh
ANDROID_SDK_TAR_GZ=android-sdk-linux.tar.gz
JDK_8_TAR_GZ=jdk-8u144-linux-arm32-vfp-hflt.tar.gz
GRADLE_ZIP=gradle-3.5-bin.zip
YA_DISK_ANDROID=https://yadi.sk/d/pMjiyCco3Mkht5
YA_DISK_JDK_8=https://yadi.sk/d/AaOqz5B23MMNv2
YA_DISK_GRADLE=https://yadi.sk/d/4w3pXL8P3Mkb8s
mkdir -p archives
wget $YA_DISK_DIRECT/$YA_DISK_ANDROID_SDK -O archives/$ANDROID_SDK_TAR_GZ
wget $YA_DISK_DIRECT/$YA_DISK_JDK_8 -O archives/$JDK_8_TAR_GZ
wget $YA_DISK_DIRECT/$YA_DISK_GRADLE -O archives/$GRADLE_ZIP
