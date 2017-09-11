#!/bin/sh
ANDROID_SDK_TAR_GZ={androidSdkTarGz}
JDK_8_TAR_GZ={jdk8TarGz}
GRADLE_ZIP={gradleZip}
YA_DISK_ANDROID_SDK=https://yadi.sk/d/pMjiyCco3Mkht5
YA_DISK_JDK_8=https://yadi.sk/d/AaOqz5B23MMNv2
YA_DISK_GRADLE=https://yadi.sk/d/4w3pXL8P3Mkb8s
YA_DISK_DIRECT=https://getfile.dokpub.com/yandex/get
mkdir -p archives
wget $YA_DISK_DIRECT/$YA_DISK_ANDROID_SDK -O archives/$ANDROID_SDK_TAR_GZ
wget $YA_DISK_DIRECT/$YA_DISK_JDK_8 -O archives/$JDK_8_TAR_GZ
wget $YA_DISK_DIRECT/$YA_DISK_GRADLE -O archives/$GRADLE_ZIP
