#!/bin/bash
#
# Basic environment variables for i2b2
# 
# Invocation within another sh script should be:
# source $I2B2_INSTALL_HOME/setenv.sh
#
#-------------------------------------------------------------------
source $I2B2_INSTALL_HOME/config/defaults.sh

if [ -z $I2B2_INSTALL_DEFAULTS_DEFINED ]
then
	I2B2_INSTALL_DEFAULTS_DEFINED=true
	export I2B2_INSTALL_DEFAULTS_DEFINED
	
	export JOB_LOG_NAME DB_TYPE 
	export ACQUISITIONS_DIRECTORY SOURCE_DIRECTORY DATA_DIRECTORY
	export ACQUISITION_BASE_URL SOURCE_ZIP DATA_ZIP AXIS_WAR
	export HTML_LOCATION LIST_SERVICES_URL FILE_REPO_LOCATION

# Should these 3 be here or not?	
	export JBOSS_HOME
	export ANT_HOME
	export JAVA_HOME

# If the adiminstrator has opted for a separate workspace, we export it...	
	if [ ! -z $I2B2_INSTALL_WORKSPACE ]
	then
		export I2B2_INSTALL_WORKSPACE 
	fi
	
fi


