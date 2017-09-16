#!/bin/sh
ANDROID_SDK_TAR_GZ={androidSdkTarGz}
JDK_8_TAR_GZ={jdk8TarGz}
GRADLE_ZIP={gradleZip}
LINK_ANDROID_SDK=https://goo.gl/3TKVwk
LINK_JDK_8=https://goo.gl/PqjDgU
LINK_GRADLE=https://goo.gl/o1SmWp
mkdir -p archives
wget $LINK_ANDROID_SDK -O archives/$ANDROID_SDK_TAR_GZ
wget $LINK_JDK_8 -O archives/$JDK_8_TAR_GZ
wget $LINK_GRADLE -O archives/$GRADLE_ZIP
