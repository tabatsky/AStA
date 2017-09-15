echo 'firstMountAndConfigureDebian script started'
# Пароль для доступа по SSH:
PASSWORD={password}
# Точка монтирования Debian:
MNTPT={mntpt}
# Путь к карте памяти:
STORAGE={storage}
# Образ Debian:
IMG_FILE=$STORAGE/debianOnAndroid.img
# Версия Debian:
DEBIAN_VERSION={debianVersion}
# Создаем точку монтирования:
mkdir -p $MNTPT
# Монтируем образ:
busybox mount -o loop $IMG_FILE $MNTPT
# Нужно для корректной работы chroot:
export PATH=/sbin:/usr/sbin:/bin:/usr/bin:/system/bin:/system/xbin:/su/bin:/su/xbin
# Второй этап debootstrap:
chroot $MNTPT /debootstrap/debootstrap --second-stage
# Настраиваем apt:
echo "deb http://ftp.se.debian.org/debian $DEBIAN_VERSION main contrib non-free" > $MNTPT/etc/apt/sources.list
# Монтируем:
busybox mount -t proc none $MNTPT/proc
busybox mount -t sysfs none $MNTPT/sys
busybox mount -o bind /dev $MNTPT/dev
busybox mount -t devpts none $MNTPT/dev/pts
export TMPDIR=/tmp
#chroot $MNTPT /bin/bash
# Чтобы apt мог работать с сетью, нужно перевести его в группу Internet (3003)
chroot $MNTPT sed -i 's#_apt:x:104:65534::/nonexistent:/bin/false#_apt:x:104:3003::/nonexistent:/bin/false#' /etc/passwd
# Устанавливаем пакеты, которые нам понадобятся:
chroot $MNTPT apt-get update
chroot $MNTPT apt-get --yes upgrade
chroot $MNTPT apt-get --yes install ne openssh-server unzip \
 android-sdk-build-tools android-sdk-platform-tools
# Устанавливаем пароль для доступа по SSH:
echo "echo 'root:$PASSWORD' | chpasswd" > $MNTPT/root/setpasswd.sh
chroot $MNTPT /bin/sh /root/setpasswd.sh
cat $MNTPT/root/setpasswd.sh
# Заменяем строчку в конфиге SSH, чтобы работал доступ под root'ом:
chroot $MNTPT sed -i '/PermitRootLogin without-password/c\PermitRootLogin yes' /etc/ssh/sshd_config 
chroot $MNTPT /etc/init.d/ssh restart
# Создаем папку AStA в Debian и монтируем на нее папку Android:
mkdir -p $MNTPT/AStA
busybox mount -o bind $STORAGE/AStA $MNTPT/AStA
# Имена файлов архивов:
ANDROID_SDK_TAR_GZ={androidSdkTarGz}
JDK_8_TAR_GZ={jdk8TarGz}
GRADLE_ZIP={gradleZip}
# Распаковываем архивы:
chroot $MNTPT tar -xzvf /AStA/archives/$ANDROID_SDK_TAR_GZ -C /opt 
chroot $MNTPT tar -xzvf /AStA/archives/$JDK_8_TAR_GZ -C /opt
chroot $MNTPT unzip /AStA/archives/$GRADLE_ZIP -d /opt
#
echo 'firstMountAndConfigureDebian script done'
