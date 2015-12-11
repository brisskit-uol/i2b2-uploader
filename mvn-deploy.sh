#!/bin/bash
#-----------------------------------------------------------------------------------------------
# This script ensures a top level licence agreement is folded into the jar.
#
# Author: Jeff Lusted (jeff.lusted@gmail.com)
#-----------------------------------------------------------------------------------------------
rm ./src/main/resources/licence.doc >/dev/null 2>/dev/null
cp ./licence.doc ./src/main/resources 
mvn clean deploy
rm -f ./src/main/resources/licence.doc 