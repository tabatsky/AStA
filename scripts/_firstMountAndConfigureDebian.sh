PASSWORD={password}
MNTPT={mntpt}
STORAGE={storage}
IMG_FILE=$STORAGE/debianOnAndroid.img
DEBIAN_VERSION={debianVersion}
mkdir -p $MNTPT
busybox mount -o loop $IMG_FILE $MNTPT
export PATH=/sbin:/usr/sbin:/bin:/usr/bin:/system/bin:/system/xbin:/su/bin:/su/xbin
chroot $MNTPT /debootstrap/debootstrap --second-stage
echo "deb http://ftp.se.debian.org/debian $DEBIAN_VERSION main contrib non-free" > $MNTPT/etc/apt/sources.list
busybox mount -t proc none $MNTPT/proc
busybox mount -t sysfs none $MNTPT/sys
busybox mount -o bind /dev $MNTPT/dev
busybox mount -t devpts none $MNTPT/dev/pts
export TMPDIR=/tmp
#chroot $MNTPT /bin/bash
chroot $MNTPT sed -i 's#_apt:x:104:65534::/nonexistent:/bin/false#_apt:x:104:3003::/nonexistent:/bin/false#' /etc/passwd
chroot $MNTPT apt-get update
chroot $MNTPT apt-get --yes upgrade
chroot $MNTPT apt-get --yes install ne openssh-server unzip \
 android-sdk-build-tools android-sdk-platform-tools
echo "echo 'root:$PASSWORD' | chpasswd" > $MNTPT/root/setpasswd.sh
chroot $MNTPT /bin/sh /root/setpasswd.sh
cat $MNTPT/root/setpasswd.sh
chroot $MNTPT sed -i '/PermitRootLogin without-password/c\PermitRootLogin yes' /etc/ssh/sshd_config 
chroot $MNTPT /etc/init.d/ssh restart
mkdir -p $MNTPT/AStA
busybox mount -o bind $STORAGE/AStA $MNTPT/AStA
ANDROID_SDK_TAR_GZ={androidSdkTarGz}
JDK_8_TAR_GZ={jdk8TarGz}
GRADLE_ZIP={gradleZip}
chroot $MNTPT tar -xzvf /AStA/archives/$ANDROID_SDK_TAR_GZ -C /opt 
chroot $MNTPT tar -xzvf /AStA/archives/$JDK_8_TAR_GZ -C /opt
chroot $MNTPT unzip /AStA/archives/$GRADLE_ZIP -d /opt
exit
