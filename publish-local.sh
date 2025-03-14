#!/bin/sh
./gradlew -Pskip-signing=true test publishToMavenLocal
exit 0
