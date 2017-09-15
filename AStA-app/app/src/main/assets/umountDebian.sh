MNTPT=/data/local/debianOnAndroid
echo "# export PATH=/sbin:/usr/sbin:/bin:/usr/bin:/system/bin:/system/xbin:/su/bin:/su/xbin"
export PATH=/sbin:/usr/sbin:/bin:/usr/bin:/system/bin:/system/xbin:/su/bin:/su/xbin
echo "# busybox umount $MNTPT/AStA"
busybox umount $MNTPT/AStA
echo "# chroot $MNTPT /etc/init.d/ssh stop"
chroot $MNTPT /etc/init.d/ssh stop
echo "# busybox umount $MNTPT/dev/pts"
busybox umount $MNTPT/dev/pts
echo "# busybox umount $MNTPT/dev"
busybox umount $MNTPT/dev
echo "# busybox umount $MNTPT/proc"
busybox umount $MNTPT/proc
echo "# busybox umount $MNTPT/sys"
busybox umount $MNTPT/sys
echo "# busybox umount $MNTPT"
busybox umount $MNTPT
echo "done"
#busybox mount


