#!/bin/bash

PWD=$(pwd)
unamestr=$(uname)
SCRIPTFILE="undefined"

while [ "$1" != "" ]; do
    case $1 in
        "local" ) TARGET_ENV=local
                                ;;
        "dev" ) TARGET_ENV=dev
                                ;;
        "kete" ) TARGET_ENV=kete
                                ;;
        "test" ) TARGET_ENV=test
                                ;;
        "prod" ) TARGET_ENV=prod
                                ;;
        -t | --tag )            IMAGE_TAG="$2"
                                shift
                                ;;
        -p | --push )           push=1
                                ;;
        -d | --no-deps )        nodeps=1
                                ;;
        -h | --help )           usage
                                exit
                                ;;
        * )                     usage
                                echo $1
                                exit 1
    esac
    shift
done

# check os, os x users install coreutils: brew install coreutils
if [ $unamestr == "Linux" ]; then
  SCRIPTFILE=$(readlink -f "$0")
elif [ $unamestr == "Darwin" ]; then
  SCRIPTFILE=$(greadlink -f "$0")
fi

SCRIPTPATH=$(dirname "$SCRIPTFILE")

cd ${SCRIPTPATH}/..

#build
if [ "$nodeps" = "1" ]; then
        mvn clean package
        mkdir -p target/site
else
        mvn clean package project-info-reports:dependencies
fi

unzip mobileauth-impl/target/mobileauth-impl-0.5.1-SNAPSHOT-bin.zip -d mobileauth-impl/target/

cd ${PWD}