#!/bin/bash
# simple build sh to build a apk check folder and sign ...set on yours .bashrc to call this sh from anywhere alias bt='/home/user/this.sh'

#clean strings
#find . -path '**' -name '*.xml' -exec sed  --in-place "/\"twrp\"/d" '{}' \;

#colors
RED='\033[1;31m'
CYAN='\033[1;36m' 
GREEN='\033[1;32m' 
YELLOW='\033[1;33m' 
NC='\033[1m'
#timer counter
START=$(date +%s.%N);
START2="$(date)";
echo -e "\n Script start $(date)\n";

#Folders Folder= you app folder SDK_Folder android sdk folder Download it if you don't have it, don't remove the sdk.dir= from the line

FOLDER=$HOME/android/KA27;
SDK_FOLDER="$HOME"/android/sdk;
SDK_DIR="sdk.dir=$SDK_FOLDER";

TOOLVERSION=$(grep buildTools "$FOLDER"/versions.gradle | head -n1 | cut -d\' -f2);
ZIPALIGN_FOLDER=$SDK_FOLDER/build-tools/$TOOLVERSION/zipalign;
KEY_FOLDER="$HOME"/android/temp/sign/fgl_pem.key;
KEY_PASS=$(<"$HOME"/android/temp/sign/pass);

#build the app BAPP=1?
BAPP=1;

# Export Java path in some machines is necessary put your java path
#export JAVA_HOME="/usr/lib/jvm/java-7-openjdk-amd64/"

# Auto sign apk Download from my folder link below extract and set the folder below on yours machine
# https://www.androidfilehost.com/?fid=312978532265364585
SIGN_FOLDER=$HOME/android/ZipScriptSign;

# out app folder and out app name

OUT_FOLDER=$FOLDER/app/build/outputs/apk/release;
APP_FINAL_NAME=KernelAdiutor.apk;

#making start here...
contains() {
    string="$1"
    substring="$2"
    if test "${string#*$substring}" != "$string"
    then
        return 0    # $substring is in $string
    else
        return 1    # $substring is not in $string
    fi
}

cd $FOLDER;

if [ ! -e ./local.properties ]; then
	echo -e "$\n local.properties not found...\nMaking a local.properties files using script information\n
\n local.properties done starting the build";
	touch $FOLDER.local.properties;
	echo $SDK_FOLDER > local.properties;
fi;
localproperties=`cat local.properties`;
if [ $localproperties != $SDK_FOLDER ]; then
	echo -e "\nSDK folder set as \n$SDK_FOLDER in the script \nbut local.properties file content is\n$localproperties\nfix it using script value";
	rm -rf .local.properties;
	touch $FOLDER.local.properties;
	echo $SDK_FOLDER > local.properties;
fi;

if [ $BAPP == 1 ]; then
./gradlew clean
echo -e "\n The above is just the cleaning build start now\n";
./gradlew build --warning-mode all 2>&1 | tee build_log.txt
fi;

END2="$(date)";
END=$(date +%s.%N);

if [ ! -e $OUT_FOLDER/app-release-unsigned.apk ]; then
	echo -e "\n${bldred}App not build${txtrst}\n"
	exit 1;
else
	echo -e "\n${bldred}Signing the App${txtrst}\n"
    jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -storepass "$KEY_PASS" -keystore "$KEY_FOLDER" "$OUT_FOLDER"/app-release-unsigned.apk Felipe_Leon
	"$ZIPALIGN_FOLDER" -v 4 "$OUT_FOLDER"/app-release-unsigned.apk "$OUT_FOLDER"/"$APP_FINAL_NAME"
	cp "$OUT_FOLDER"/"$APP_FINAL_NAME" "$OUT_FOLDER"/ka"$(date +%s)".apk
	
	echo "$(./gradlew -q gradleUpdates | sed '/jacoco/d')" >> build_log.txt

        ISSUES=$(grep issues build_log.txt | grep release)
	if [ -n "$ISSUES" ]; then
		NOISSUES=0;
		contains "$ISSUES" ": 11 issues" && NOISSUES=1;
		if [ $NOISSUES == 0 ]; then
			echo -e "\n${CYAN}Lint issues:\n${NC}";
			echo -e "${RED}$ISSUES${NC}";
			sensible-browser "$FOLDER"/app/build/reports/lint-results.html
		fi;
	fi;

        DEPRECATION=$(grep deprecation build_log.txt)
	if [ -n "$DEPRECATION" ]; then
		echo -e "\n${CYAN}Build deprecation:\n${NC}";
		echo -e "${RED}$DEPRECATION${NC}";
	fi;

        UPDATEDEPENDENCIES=$(grep ' \-> ' build_log.txt)
	if [ -n "$UPDATEDEPENDENCIES" ]; then
		echo -e "\n${CYAN}Dependencies that need update:\n${NC}";
		echo -e "${RED}$UPDATEDEPENDENCIES${NC}";
	fi;

	echo -e "\n${GREEN}App saved at $OUT_FOLDER"/"$APP_FINAL_NAME${NC}";

fi;

echo -e "\n${YELLOW}*** Build END ***\n";
echo -e "Total elapsed time of the script: ${RED}$(echo "($END - $START) / 60"|bc ):$(echo "(($END - $START) - (($END - $START) / 60) * 60)"|bc ) ${YELLOW}(minutes:seconds).\n${NC}";
exit 1;
