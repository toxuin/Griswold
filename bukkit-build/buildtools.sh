#!/bin/bash
file=bukkit.jar
if [ ! -f $file ]; then
    mkdir build && cd build
    wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar -O BuildTools.jar && java -jar BuildTools.jar --rev $BUKKIT_VER
    cp craftbukkit-* ../"$file"
    cd .. && rm -rf build
fi
