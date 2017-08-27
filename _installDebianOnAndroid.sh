#!/bin/sh
# Чтобы не спрашивало пароль в середине скрипта:
sudo echo 'installDebianOnAndroid script started'
# Архитектура устройства
ARCH={arch}
# Версия Debian
DEBIAN_VERSION={debianVersion}
# Путь к карте памяти на устройстве:
STORAGE={storage}
# Место, где мы создадим образ на декстопе:
DATA_DRIVE={dataDrive}
# Для совместимости с файловой системой FAT32 
# размер файла образа не должен превышать (2^32 - 1) байт
BS=1M
COUNT=4095
dd if=/dev/zero of=$DATA_DRIVE/debianOnAndroid.img bs=$BS count=$COUNT
mkdir -p ~/debianOnAndroid
sudo mkfs.ext3 $DATA_DRIVE/debianOnAndroid.img
sudo mount -o user,loop,exec,dev $DATA_DRIVE/debianOnAndroid.img ~/debianOnAndroid/
sudo debootstrap --verbose --arch $ARCH --foreign $DEBIAN_VERSION ~/debianOnAndroid/ http://ftp.se.debian.org/debian
sudo umount $DATA_DRIVE/debianOnAndroid.img
adb push $DATA_DRIVE/debianOnAndroid.img $STORAGE
# Busybox лучше заранее установить через приложение:
# https://play.google.com/store/apps/details?id=stericson.busybox&hl=ru&rdid=stericson.busybox
#adb push busybox/busybox-armv6l /sdcard/busybox
#adb shell su -c cp /sdcard/busybox /data/local/busybox
#adb shell su -c chmod 755 /data/local/busybox
# Скрипт для первого монтирования и настройки Debian
adb push firstMountAndConfigureDebian.sh $STORAGE
# Скрипты для монтирования и размонтирования
adb push mountDebian.sh $STORAGE
adb push umountDebian.sh $STORAGE
adb shell mkdir -p $STORAGE/AStA
# Заранее приготовленные архивы JDK, Android SDK и gradle
ANDROID_SDK_TAR_GZ={androidSdkTarGz} 
JDK_8_TAR_GZ=jdk-8u144-linux-arm32-vfp-hflt.tar.gz
GRADLE_ZIP=gradle-4.1-bin.zip
adb push prebuilt/$ANDROID_SDK_TAR_GZ $STORAGE/AStA
adb push prebuilt/$JDK_8_TAR_GZ $STORAGE/AStA
adb push prebuilt/$GRADLE_ZIP $STORAGE/AStA
adb push gradleExec.sh $STORAGE/AStA
# Запускаем скрипт первоначальной настройки Debian:
adb shell su -c sh $STORAGE/firstMountAndConfigureDebian.sh
