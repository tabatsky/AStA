MNTPT=/data/local/debianOnAndroid
busybox umount $MNTPT/AStA
chroot $MNTPT /etc/init.d/ssh stop
busybox umount $MNTPT/dev/pts
busybox umount $MNTPT/dev
busybox umount $MNTPT/proc
busybox umount $MNTPT/sys
busybox umount $MNTPT
exit