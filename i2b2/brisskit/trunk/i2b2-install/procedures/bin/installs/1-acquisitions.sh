#!/bin/bash
#-----------------------------------------------------------------------------------------------
# Acquires i2b2 data and source zip files plus the axis2.war file.
# Unzips the data and source zip files into appropriate directories within the workspace.
#
# Mandatory: the I2B2_INSTALL_HOME environment variable to be set.
# Optional : the I2B2_INSTALL_WORKSPACE environment variable.
# The latter is an optional full path to a workspace area. If not set, defaults to a workspace
# within the install home.
#
# USAGE: 1-acquisitions.sh job-name
# Where: 
#   job-name is a suitable tag to group all jobs associated with the overall workflow
# Notes:
#   The job-name is used to create a working directory for the overall workflow; eg:
#   I2B2_INSTALL_HOME/job-name
#   This working directory is created if it does not exist.
#
# Further tailoring can be achieved via the defaults.sh script.
#
# Author: Jeff Lusted (jl99@leicester.ac.uk)
#-----------------------------------------------------------------------------------------------
source $I2B2_INSTALL_HOME/bin/common/functions.sh
source $I2B2_INSTALL_HOME/bin/common/setenv.sh

print_project_install_usage() {
   echo " USAGE: 1-acquisitions.sh job-name"
   echo " Where:"
   echo "   job-name is a suitable tag to group all jobs associated with the overall workflow"
   echo " Notes:"
   echo "   The job-name is used to create a working directory for the overall workflow; eg:"
   echo "   I2B2_INSTALL_HOME/job-name"
   echo "   This working directory is created if it does not exist."
}

#=======================================================================
# First, some basic checks...
#=======================================================================
#
# Check on the usage...
if [ ! $# -eq 1 ]
then
	echo "Error! Incorrect number of arguments."
	echo ""
	print_project_install_usage
	exit 1
fi

#
# Retrieve job-name into its variable...
JOB_NAME=$1

#
# It is possible to set your own procedures workspace.
# But if it doesn't exist, we create one for you within the procedures home...
if [ -z $I2B2_INSTALL_WORKSPACE ]
then
	I2B2_INSTALL_WORKSPACE=$I2B2_INSTALL_HOME/work
fi

#
# Establish a log file for the job...
LOG_FILE=$I2B2_INSTALL_WORKSPACE/$JOB_NAME/$JOB_LOG_NAME

#
# If required, create a working directory for this job step.
ACQUISITIONS=$I2B2_INSTALL_WORKSPACE/$JOB_NAME/$ACQUISITIONS_DIRECTORY
if [ ! -d $ACQUISITIONS ]
then
	mkdir -p $ACQUISITIONS
	exit_if_bad $? "Failed to create working directory $ACQUISITIONS"
fi

#===========================================================================
# Print a banner for this step of the job.
#===========================================================================
print_banner "1-acquisitions.sh" $JOB_NAME $LOG_FILE 

#===========================================================================
# The real work is about to start.
# Give the user a warning...
#=========================================================================== 
print_message "About to acquire source and data zip files for i2b2" $LOG_FILE
echo "This may take some time."

print_message "" $LOG_FILE
print_message "First: acquiring axis war file..." $LOG_FILE
if [ -e $ACQUISITIONS/$AXIS_WAR ]
then
	print_message "Bypassing, file already exists." $LOG_FILE
else
	wget -O $ACQUISITIONS/$AXIS_WAR $ACQUISITION_BASE_URL/$AXIS_WAR
	exit_if_bad $? "Failed to acquire axis war file." $LOG_FILE
	print_message "Success! Acquired axis war file." $LOG_FILE
fi

print_message "" $LOG_FILE
print_message "Second: acquiring i2b2 core source zip file..." $LOG_FILE
if [ -e $ACQUISITIONS/$SOURCE_ZIP ]
then
	print_message "Bypassing, file already exists." $LOG_FILE
else
	wget -O $ACQUISITIONS/$SOURCE_ZIP $ACQUISITION_BASE_URL/$SOURCE_ZIP
	exit_if_bad $? "Failed to acquire i2b2 core source zip file." $LOG_FILE
	print_message "Success! Acquired i2b2 core source zip file." $LOG_FILE
fi
  
print_message "" $LOG_FILE
print_message "Third: acquiring i2b2 data zip file..." $LOG_FILE
if [ -e $ACQUISITIONS/$DATA_ZIP ]
then
	print_message "Bypassing, file already exists." $LOG_FILE
else
	wget -O $ACQUISITIONS/$DATA_ZIP $ACQUISITION_BASE_URL/$DATA_ZIP
	exit_if_bad $? "Failed to acquire i2b2 data zip file." $LOG_FILE
	print_message "Success! Acquired i2b2 data zip file." $LOG_FILE
fi

print_message "" $LOG_FILE
print_message "Unzipping i2b2 core source zip file." $LOG_FILE
unzip $ACQUISITIONS/$SOURCE_ZIP -d $I2B2_INSTALL_WORKSPACE/$JOB_NAME/$SOURCE_DIRECTORY >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to unzip i2b2 core source zip file: $ACQUISITIONS/$SOURCE_ZIP" $LOG_FILE

print_message "" $LOG_FILE
print_message "Unzipping i2b2 data zip file." $LOG_FILE
unzip $ACQUISITIONS/$DATA_ZIP -d $I2B2_INSTALL_WORKSPACE/$JOB_NAME/$DATA_DIRECTORY >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to unzip i2b2 data zip file: $ACQUISITIONS/$SOURCE_ZIP" $LOG_FILE

#=========================================================================
# If we got this far, we must be successful...
#=========================================================================
print_footer "1-acquisitions.sh" $JOB_NAME $LOG_FILE