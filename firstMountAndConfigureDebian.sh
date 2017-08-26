PASSWORD=1234567
MNTPT=/data/local/debianOnAndroid
STORAGE=/sdcard
IMG_FILE=$STORAGE/debianOnAndroid.img
DEBIAN_VERSION=wheezy
mkdir -p $MNTPT
busybox mount -o loop $IMG_FILE $MNTPT
export PATH=/sbin:/usr/sbin:/bin:/usr/bin:/system/bin:/system/xbin
chroot $MNTPT /debootstrap/debootstrap --second-stage
echo "deb http://ftp.se.debian.org/debian $DEBIAN_VERSION main contrib non-free" > $MNTPT/etc/apt/sources.list
busybox mount -t proc none $MNTPT/proc
busybox mount -t sysfs none $MNTPT/sys
busybox mount -o bind /dev $MNTPT/dev
busybox mount -t devpts none $MNTPT/dev/pts
export TMPDIR=/tmp
chroot $MNTPT apt-get update
chroot $MNTPT apt-get --yes upgrade
chroot $MNTPT apt-get --yes install ne openssh-server unzip # \
 #libc6-dev \
 #git gnupg flex bison gperf build-essential \
 #zip curl libncurses5-dev x11proto-core-dev \
 #libx11-dev libreadline6-dev libgl1-mesa-glx libgl1-mesa-dev \
 #python-markdown libxml2-utils xsltproc zlib1g-dev
 #android-sdk-platform-tools android-sdk-build-tools gradle
echo "echo 'root:$PASSWORD' | chpasswd" > $MNTPT/root/setpasswd.sh
chroot $MNTPT /bin/sh /root/setpasswd.sh
cat $MNTPT/root/setpasswd.sh
chroot $MNTPT sed -i '/PermitRootLogin without-password/c\PermitRootLogin yes' /etc/ssh/sshd_config 
chroot $MNTPT /etc/init.d/ssh restart
#chroot $MNTPT /bin/bash
mkdir -p $STORAGE/AStA
mkdir -p $MNTPT/AStA
busybox mount -o bind $STORAGE/AStA $MNTPT/AStA
ANDROID_SDK_TAR_GZ=android-sdk-linux-armhf.tar.gz 
JDK_8_TAR_GZ=jdk-8u144-linux-arm32-vfp-hflt.tar.gz
GRADLE_ZIP=gradle-4.1-bin.zip
adb push prebuilt/$ANDROID_SDK_TAR_GZ $STORAGE/AStA
adb push prebuilt/$JDK_8_TAR_GZ $STORAGE/AStA
adb push prebuilt/$GRADLE_ZIP $STORAGE/AStA
chroot $MNTPT tar -xzvf /AStA/$ANDROID_SDK_TAR_GZ -C /opt 
chroot $MNTPT tar -xzvf /AStA/$JDK_8_TAR_GZ -C /opt
chroot $MNTPT unzip /AStA/$GRADLE_ZIP -d /opt
adb push gradleExec.sh $STORAGE/AStA
exit
