#!/bin/bash
#-----------------------------------------------------------------------------------------------
# Inserts the project's data sources for crc, ont and work cells.
#
# Mandatory: the I2B2_PROCEDURES_HOME environment variable to be set.
# Optional : the I2B2_PROCEDURES_WORKSPACE environment variable.
# The latter is an optional full path to a workspace area. If not set, defaults to a workspace
# within the procedures' home.
#
# USAGE: 2-update-datasources.sh job-name
# Where: 
#   job-name is a suitable tag to group all jobs associated with the overall workflow
# Notes:
#   The job-name is used to create a working directory for the overall workflow; eg:
#   I2B2_PROCEDURES_WORKSPACE/{job-name}
#   This working directory should already exist.
#
# Further tailoring can be achieved via the defaults.sh script.
#
# Author: Jeff Lusted (jl99@leicester.ac.uk)
#-----------------------------------------------------------------------------------------------
source $I2B2_PROCEDURES_HOME/bin/common/functions.sh
source $I2B2_PROCEDURES_HOME/bin/common/setenv.sh

print_update_datasources_usage() {
   echo " USAGE: 2-update-datasources.sh job-name"
   echo " Where:"
   echo "   job-name is a suitable tag to group all jobs associated with the overall workflow"
   echo " Notes:"
   echo "   The job-name is used to create a working directory for the overall workflow; eg:"
   echo "   I2B2_PROCEDURES_WORKSPACE/{job-name}"
   echo "   This working directory should already exist."
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
	print_update_datasources_usage
	exit 1
fi

#
# Retrieve job-name into its variable...
JOB_NAME=$1

#
# It is possible to set your own procedures workspace.
# But if it doesn't exist, we create one for you within the procedures home...
if [ -z $I2B2_PROCEDURES_WORKSPACE ]
then
	I2B2_PROCEDURES_WORKSPACE=$I2B2_PROCEDURES_HOME/work
fi

#
# Establish a log file for the job...
LOG_FILE=$I2B2_PROCEDURES_WORKSPACE/$JOB_NAME/$JOB_LOG_NAME

#
# The working directory must already exist...
WORK_DIR=$I2B2_PROCEDURES_WORKSPACE/$JOB_NAME
if [ ! -d $WORK_DIR ]
then
	print_message "Error! Working directory does not exist: $WORK_DIR"
	print_message "Please check your job name: $JOB_NAME. Exiting..."
	exit 1
fi

#===========================================================================
# Print a banner for this step of the job.
#===========================================================================
print_banner "update-datasources" $JOB_NAME $LOG_FILE 

#===========================================================================
# The real work is about to start.
# Give the user a warning...
#=========================================================================== 
print_message "Updating data sources within JBoss"
echo "   Please note detailed log messages are written to $LOG_FILE"
echo "   If you want to see this during execution, try: tail -f $LOG_FILE"
echo ""

#
# Verify JBOSS is not running.
print_message "Attempting to stop JBoss, if it is running." $LOG_FILE
$JBOSS_HOME/bin/shutdown.sh -S >>$LOG_FILE 2>>$LOG_FILE
sleep 35

# Deploy ont data source...
merge_config_properties \
             $I2B2_PROCEDURES_HOME/config/config.properties \
             $I2B2_PROCEDURES_HOME/config/$DB_TYPE/ont-ds.xml \
             $JBOSS_HOME/server/default/deploy/$ONT_DS   
exit_if_bad $? "Failed to deploy ds at $JBOSS_HOME/server/default/deploy/$ONT_DS" $LOG_FILE   

# Deploy work data source...
merge_config_properties \
             $I2B2_PROCEDURES_HOME/config/config.properties \
             $I2B2_PROCEDURES_HOME/config/$DB_TYPE/work-ds.xml \
             $JBOSS_HOME/server/default/deploy/$WORK_DS 
exit_if_bad $? "Failed to deploy ds at $JBOSS_HOME/server/default/deploy/$WORK_DS" $LOG_FILE 

# Deploy crc data source...
merge_config_properties \
             $I2B2_PROCEDURES_HOME/config/config.properties \
             $I2B2_PROCEDURES_HOME/config/$DB_TYPE/crc-ds.xml \
             $JBOSS_HOME/server/default/deploy/$CRC_DS
exit_if_bad $? "Failed to deploy ds at $JBOSS_HOME/server/default/deploy/$CRC_DS" $LOG_FILE  

#====================================
# START JBOSS (as a background task)
#====================================
print_message "" $LOG_FILE
print_message "Starting JBoss in the background." $LOG_FILE
$JBOSS_HOME/bin/run.sh -b 0.0.0.0 >>$LOG_FILE 2>>$LOG_FILE &

sleep 35
#=========================================================================
# If we got this far, we may have been be successful...
#=========================================================================
print_message "Data sources updated." $LOG_FILE
print_footer "update-datasources" $JOB_NAME $LOG_FILE