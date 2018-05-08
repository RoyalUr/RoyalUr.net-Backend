#!/bin/bash
clear
cd "$( dirname "$0" )"
cd ..
if mvn install
then
  cd server
  clear
  cp ../target/RoyalUrServer-jar-with-dependencies.jar RoyalUrServer.jar
  java -Xms1024M -Xmx1024M -jar RoyalUrServer.jar -o true
fi