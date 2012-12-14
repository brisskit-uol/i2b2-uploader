#!/bin/bash
#------------------------------------------------------------------------------------------------------------
# Utility to create symbolic links for all routine procedures within the bin directory and below.
# The links are created in the $I2B2_INSTALL_HOME/bin/symlinks directory.
#
# Allows a simple amendment if you want to change the PATH environment variable...
# set PATH=$I2B2_INSTALL_HOME/bin/symlinks:$PATH
# export PATH
#
# Mandatory: the following environment variable must be set
#            I2B2_INSTALL_HOME          
#
# USAGE: build-symlinks.sh
#
# Author: Jeff Lusted (jl99@leicester.ac.uk)
#-------------------------------------------------------------------------------------------------------------
source $I2B2_INSTALL_HOME/bin/common/functions.sh
source $I2B2_INSTALL_HOME/bin/common/setenv.sh

process_directory() {
	for f in $1/*
	do
		if [ -f $f ]
		then
			ln -s $f  $I2B2_INSTALL_HOME/bin/symlinks/
			exit_if_bad $? "Failed to create a symbolic link for $f"
		fi
	done
}

#
# The bin directory must exist!
if [ ! -d $I2B2_INSTALL_HOME/bin ]
then
	echo "Error! Could not find the bin directory: $I2B2_INSTALL_HOME/bin"
	exit 1
fi

process_directory $I2B2_INSTALL_HOME/bin/compositions
process_directory $I2B2_INSTALL_HOME/bin/installs

echo "Done!"



 