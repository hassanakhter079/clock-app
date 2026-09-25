#!/bin/bash
set -e

PROJECT_DIR="/root/clock-app"
cd "$PROJECT_DIR"

mkdir -p "$PROJECT_DIR/build/obj"
mkdir -p "$PROJECT_DIR/build/apk"
rm -rf "$PROJECT_DIR/build/obj/*" "$PROJECT_DIR/build/apk/*"

VERSION="1.3.0"

echo "0. Generating dope AMOLED neon icons..."
python3 make_icons.py 2>/dev/null || true
python3 create_dope_icon.py

echo "1. Generating R.java..."
aapt package -f -m -J src -M AndroidManifest.xml -S res -I /root/android-build-tools/android.jar

echo "2. Compiling Java sources..."
javac -source 8 -target 8 -bootclasspath /root/android-build-tools/android.jar -cp /root/android-build-tools/android.jar -d build/obj $(find src -name "*.java")

echo "3. Converting to DEX bytecode (D8)..."
java -cp /root/android-build-tools/r8.jar com.android.tools.r8.D8 --min-api 21 --lib /root/android-build-tools/android.jar --output build/apk $(find build/obj -name "*.class")

echo "4. Packaging APK resources & assets..."
aapt package -f -M AndroidManifest.xml -S res -A assets -I /root/android-build-tools/android.jar -F build/unaligned.apk

echo "5. Adding classes.dex to APK..."
cd build/apk
aapt add ../unaligned.apk classes.dex
cd "$PROJECT_DIR"

echo "6. Zipaligning APK..."
zipalign -f -p 4 build/unaligned.apk build/aligned.apk

echo "7. Signing APK..."
apksigner sign --ks /root/android-build-tools/release.keystore --ks-pass pass:android --key-pass pass:android --out "build/Clock_v${VERSION}.apk" build/aligned.apk
cp "build/Clock_v${VERSION}.apk" build/Clock.apk

echo "8. Verifying signature..."
apksigner verify "build/Clock_v${VERSION}.apk"

echo "9. Deploying to storage and output directories..."
cp "build/Clock_v${VERSION}.apk" "/root/Clock_v${VERSION}.apk"
cp build/Clock.apk /root/Clock.apk

mkdir -p "/storage/emulated/0/Apps by antigravity/Clock"
cp "build/Clock_v${VERSION}.apk" "/storage/emulated/0/Apps by antigravity/Clock/Clock_v${VERSION}.apk"
cp build/Clock.apk "/storage/emulated/0/Apps by antigravity/Clock/Clock.apk"
cp assets/index.html "/storage/emulated/0/Apps by antigravity/Clock/index.html"
cp assets/index.html "/storage/emulated/0/Apps by antigravity/Clock/clock.html"

mkdir -p "/storage/emulated/0/Apps by antigravity/Clock/fonts"
cp -r assets/fonts/* "/storage/emulated/0/Apps by antigravity/Clock/fonts/"

echo "10. Silently installing via Shizuku..."
/usr/local/bin/shizuku-install "/storage/emulated/0/Apps by antigravity/Clock/Clock_v${VERSION}.apk"

echo "SUCCESS! APK built, deployed, and silently installed to:"
echo " - /storage/emulated/0/Apps by antigravity/Clock/Clock_v${VERSION}.apk"
echo " - /storage/emulated/0/Apps by antigravity/Clock/Clock.apk"
echo " - /root/Clock_v${VERSION}.apk"
