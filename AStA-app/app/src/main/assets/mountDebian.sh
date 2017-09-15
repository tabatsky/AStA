MNTPT=/data/local/debianOnAndroid
ASTA_FOLDER={astaFolder}
IMG_FILE={imgFile}
echo "# mkdir -p $MNTPT"
mkdir -p $MNTPT
echo "# export PATH=/sbin:/usr/sbin:/bin:/usr/bin:/system/bin:/system/xbin:/su/bin:/su/xbin"
export PATH=/sbin:/usr/sbin:/bin:/usr/bin:/system/bin:/system/xbin:/su/bin:/su/xbin
echo "# busybox mount -o loop $IMG_FILE $MNTPT"
busybox mount -o loop $IMG_FILE $MNTPT
echo "# busybox mount -t proc none $MNTPT/proc"
busybox mount -t proc none $MNTPT/proc
echo "# busybox mount -t sysfs none $MNTPT/sys"
busybox mount -t sysfs none $MNTPT/sys
echo "# busybox mount -o bind /dev $MNTPT/dev"
busybox mount -o bind /dev $MNTPT/dev
echo "# busybox mount -t devpts none $MNTPT/dev/pts"
busybox mount -t devpts none $MNTPT/dev/pts
echo "# export TMPDIR=/tmp"
export TMPDIR=/tmp
#chroot $MNTPT bash
echo "# chroot $MNTPT /etc/init.d/ssh start"
chroot $MNTPT /etc/init.d/ssh start
echo "# mkdir -p $MNTPT/AStA"
mkdir -p $MNTPT/AStA
echo "# busybox mount -o bind $ASTA_FOLDER $MNTPT/AStA"
busybox mount -o bind $ASTA_FOLDER $MNTPT/AStA
#echo "ls $MNTPT"
#ls $MNTPT
echo "done"
#busybox mount
