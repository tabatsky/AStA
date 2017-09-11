MNTPT={mntpt}
STORAGE={storage}
IMG_FILE=$STORAGE/debianOnAndroid.img
mkdir -p $MNTPT
busybox mount -o loop $IMG_FILE $MNTPT
export PATH=/sbin:/usr/sbin:/bin:/usr/bin:/system/bin:/system/xbin:/su/bin:/su/xbin
busybox mount -t proc none $MNTPT/proc
busybox mount -t sysfs none $MNTPT/sys
busybox mount -o bind /dev $MNTPT/dev
busybox mount -t devpts none $MNTPT/dev/pts
export TMPDIR=/tmp
#chroot $MNTPT bash
chroot $MNTPT /etc/init.d/ssh start
mkdir -p $STORAGE/AStA
mkdir -p $MNTPT/AStA
busybox mount -o bind $STORAGE/AStA $MNTPT/AStA
exit
