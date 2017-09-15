MNTPT=/data/local/debianOnAndroid
ASTA_FOLDER={astaFolder}

if [ ! -d "$MNTPT/bin" ]; then
    echo "Seems Debian img not mounted. Aborting..."
    exit
fi

GRADLE_EXEC=/AStA/scripts/gradleExec.sh
MODULE_DIR=/AStA/Projects/$1/$2
GRADLE_CMD=$3
echo "# export PATH=/sbin:/usr/sbin:/bin:/usr/bin:/system/bin:/system/xbin:/su/bin:/su/xbin"
export PATH=/sbin:/usr/sbin:/bin:/usr/bin:/system/bin:/system/xbin:/su/bin:/su/xbin
echo "# chroot $MNTPT sh $GRADLE_EXEC $MODULE_DIR '$GRADLE_CMD'"
chroot $MNTPT sh $GRADLE_EXEC $MODULE_DIR "$GRADLE_CMD"
echo "# mkdir -p \"$ASTA_FOLDER/apk/$1\""
mkdir -p "$ASTA_FOLDER/apk/$1"
echo "# cp $ASTA_FOLDER/Projects/$1/$2/build/outputs/apk/* $ASTA_FOLDER/apk/$1/"
cp $ASTA_FOLDER/Projects/$1/$2/build/outputs/apk/* $ASTA_FOLDER/apk/$1/