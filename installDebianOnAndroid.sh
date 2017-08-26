#!/bin/sh
# Чтобы не спрашивало пароль в середине скрипта:
sudo echo 'installDebianOnAndroid script started'
# Нужно выбрать подходящую архитектуру для Вашего устройства
ARCH=armhf
# Версия Debian
# Использую jessie, так как ядро stretch несовместимо с моим устройством
# jessie: build-tools 20.0.0
# stretch: build-tools 23.0.3
DEBIAN_VERSION=wheezy
# Путь к карте памяти на устройстве:
STORAGE=/sdcard
# Место, где мы создадим образ на декстопе:
DATA_DRIVE=/mnt/drive-F
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
# Архитектура Busybox
#BUSYBOX_ARCH=armv6l
#adb push busybox/busybox-$BUSYBOX_ARCH /sdcard/busybox
#adb shell su -c cp /sdcard/busybox /data/local/busybox
#adb shell su -c chmod 755 /data/local/busybox
# Скрипт для первого монтирования и настройки Debian
adb push firstMountAndConfigureDebian.sh $STORAGE
# Скрипты для монтирования и размонтирования
adb push mountDebian.sh $STORAGE
adb push umountDebian.sh $STORAGE
adb shell su -c sh $STORAGE/firstMountAndConfigureDebian.sh
