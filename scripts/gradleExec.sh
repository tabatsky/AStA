export JAVA_HOME=/opt/jdk1.8.0_144/
export ANDROID_HOME=/usr/local/android-sdk/
export PATH=/opt/gradle-3.5/bin:$PATH
cd $1
gradle $2
