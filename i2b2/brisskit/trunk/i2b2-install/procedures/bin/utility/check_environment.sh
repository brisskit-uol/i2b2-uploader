#!/bin/bash
#
# Checks basic environment variables required for i2b2 install procedures.
# 
# This script is for end users only!
#
#-------------------------------------------------------------------

if [ -z $I2B2_INSTALL_HOME ]
then
	echo "ERROR! I2B2_INSTALL_HOME environment variable is not set."
else
    if [ -z $I2B2_INSTALL_WORKSPACE ]
    then
	    echo "WARNING! I2B2_INSTALL_WORKSPACE is not set. Will use default setting: ${I2B2_INSTALL_HOME}/work"
    fi
fi

if [ -z $JAVA_HOME ]
then
	echo "WARNING! JAVA_HOME environment variable is not set."
fi

if [ -z $ANT_HOME ]
then
	echo "WARNING! ANT_HOME environment variable is not set."
fi

if [ -z $JBOSS_HOME ]
then
	echo "WARNING! JBOSS_HOME environment variable is not set."
fi



