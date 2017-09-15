#/bin/sh
# Пароль для доступа по SSH:
PASSWORD=1234567
# Архитектура устройства
ARCH=armhf
# Версия Debian:
DEBIAN_VERSION=stretch
# Путь к карте памяти на устройстве:
STORAGE=/sdcard
# Место, где мы создадим образ на декстопе:
DATA_DRIVE=/Data
# Имена файлов архивов:
ANDROID_SDK_TAR_GZ=android-sdk-linux.tar.gz 
JDK_8_TAR_GZ=jdk-8u144-linux-arm32-vfp-hflt.tar.gz
GRADLE_ZIP=gradle-3.5-bin.zip
# Точка монтирования Debian:
MNTPT=/data/local/debianOnAndroid

cp _firstMountAndConfigureDebian.sh firstMountAndConfigureDebian.sh
sed -i "s#{storage}#$STORAGE#" firstMountAndConfigureDebian.sh
sed -i "s#{mntpt}#$MNTPT#" firstMountAndConfigureDebian.sh
sed -i "s#{debianVersion}#$DEBIAN_VERSION#" firstMountAndConfigureDebian.sh
sed -i "s#{androidSdkTarGz}#$ANDROID_SDK_TAR_GZ#" firstMountAndConfigureDebian.sh
sed -i "s#{jdk8TarGz}#$JDK_8_TAR_GZ#" firstMountAndConfigureDebian.sh
sed -i "s#{gradleZip}#$GRADLE_ZIP#" firstMountAndConfigureDebian.sh
sed -i "s#{password}#$PASSWORD#" firstMountAndConfigureDebian.sh

cp _mountDebian.sh mountDebian.sh
sed -i "s#{storage}#$STORAGE#" mountDebian.sh
sed -i "s#{mntpt}#$MNTPT#" mountDebian.sh

cp _umountDebian.sh umountDebian.sh
sed -i "s#{mntpt}#$MNTPT#" umountDebian.sh

cp _installDebianOnAndroid.sh installDebianOnAndroid.sh
sed -i "s#{storage}#$STORAGE#" installDebianOnAndroid.sh
sed -i "s#{debianVersion}#$DEBIAN_VERSION#" installDebianOnAndroid.sh
sed -i "s#{androidSdkTarGz}#$ANDROID_SDK_TAR_GZ#" installDebianOnAndroid.sh
sed -i "s#{jdk8TarGz}#$JDK_8_TAR_GZ#" installDebianOnAndroid.sh
sed -i "s#{gradleZip}#$GRADLE_ZIP#" installDebianOnAndroid.sh
sed -i "s#{arch}#$ARCH#" installDebianOnAndroid.sh
sed -i "s#{dataDrive}#$DATA_DRIVE#" installDebianOnAndroid.sh

cp _getArchives.sh getArchives.sh
sed -i "s#{androidSdkTarGz}#$ANDROID_SDK_TAR_GZ#" getArchives.sh
sed -i "s#{jdk8TarGz}#$JDK_8_TAR_GZ#" getArchives.sh
sed -i "s#{gradleZip}#$GRADLE_ZIP#" getArchives.sh

if [ ! -f archives/$ANDROID_SDK_TAR_GZ ] || [ ! -f archives/$JDK_8_TAR_GZ ] || [ ! -f archives/$GRADLE_ZIP ]; then
    echo 'Archives not found. Downloading...'
    sh getArchives.sh
fi

sh installDebianOnAndroid.sh
