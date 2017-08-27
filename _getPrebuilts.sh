#!/bin/sh
ANDROID_SDK_TAR_GZ={androidSdkTarGz}
JDK_8_TAR_GZ={jdk8TarGz}
GRADLE_ZIP={gradleZip}
YA_DISK_ANDROID_SDK=https://yadi.sk/d/k2WCjiuL3MMN9r
YA_DISK_JDK_8=https://yadi.sk/d/AaOqz5B23MMNv2
YA_DISK_GRADLE=https://yadi.sk/d/H5eRWNq93MMNvD
YA_DISK_DIRECT=https://getfile.dokpub.com/yandex/get
mkdir -p prebuilt
wget $YA_DISK_DIRECT/$YA_DISK_ANDROID_SDK -O prebuilt/$ANDROID_SDK_TAR_GZ
wget $YA_DISK_DIRECT/$YA_DISK_JDK_8 -O prebuilt/$JDK_8_TAR_GZ
wget $YA_DISK_DIRECT/$YA_DISK_GRADLE -O prebuilt/$GRADLE_ZIP
