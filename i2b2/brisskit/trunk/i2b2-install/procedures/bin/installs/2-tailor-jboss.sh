#!/bin/bash
#-----------------------------------------------------------------------------------------------
# Tailors JBoss:
#   (i)   installs the axis2.war file
#   (ii)  sets JVM settings
#   (iii) configures ports
#
# Mandatory: the I2B2_INSTALL_HOME environment variable to be set.
# Optional : the I2B2_INSTALL_WORKSPACE environment variable.
# The latter is an optional full path to a workspace area. If not set, defaults to a workspace
# within the install home.
#
# Pre-reqs:
#   JBoss installed
#   1-acquisitions.sh has been run at some point to acquire axis2.war
#   JBoss config settings reviewed (see config/jboss directory)
#
# USAGE: 2-tailor-jboss.sh job-name
# Where: 
#   job-name is a suitable tag to group all jobs associated with the overall workflow
# Notes:
#   The job-name is used to create a work directory for the overall workflow; eg:
#   I2B2_INSTALL_HOME/job-name
#   This work directory must already exist.
#
# Further tailoring can be achieved via the defaults.sh script.
#
# Question: Should this stop and start JBoss?
#
# Author: Jeff Lusted (jl99@leicester.ac.uk)
#-----------------------------------------------------------------------------------------------
source $I2B2_INSTALL_HOME/bin/common/functions.sh
source $I2B2_INSTALL_HOME/bin/common/setenv.sh

print_project_install_usage() {
   echo " USAGE: 2-tailor-jboss.sh job-name"
   echo " Where:"
   echo "   job-name is a suitable tag to group all jobs associated with the overall workflow"
   echo " Notes:"
   echo "   The job-name is used to locate a work directory for the overall workflow; eg:"
   echo "   I2B2_INSTALL_HOME/job-name"
   echo "   This work directory must already exist."
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
WORK_DIR=$I2B2_INSTALL_WORKSPACE/$JOB_NAME
LOG_FILE=$WORK_DIR/$JOB_LOG_NAME

#
# We must already have a work directory for this job step
# (otherwise no acquisitions)...
if [ ! -d $WORK_DIR ]
then
	echo "Error! Could not find work directory."
	echo "Please check acquisitions step has been run and that job name \"$JOB_NAME\" is correct."
	exit 1
fi

#===========================================================================
# Print a banner for this step of the job.
#===========================================================================
print_banner "2-tailor-jboss.sh" $JOB_NAME $LOG_FILE 

#===========================================================================
# The real work is about to start.
# Give the user a warning...
#=========================================================================== 
echo "About to install i2b2 PM cell..."
echo ""
echo "   Please note detailed log messages are written to $LOG_FILE"
echo "   If you want to see this during execution, try: tail -f $LOG_FILE"
echo ""

#
# Check whether JBoss home directory already exists...

if [ -z $JBOSS_HOME ]
then
	print_message "ERROR! JBOSS_HOME environment variable is not set."
	exit 1
fi
if [ ! -d $JBOSS_HOME ]
then
	print_message "ERROR! Directory for JBOSS_HOME does not exist: $JBOSS_HOME"
	exit 1
fi
ls $JBOSS_HOME >/dev/null 2>/dev/null 
exit_if_bad $? "Either JBoss not installed or JBOSS_HOME not set correctly ." $LOG_FILE

#
# Overwrite the run.conf to set the correct JDK and appropriate memory settings for the java runtime...
print_message "" $LOG_FILE
print_message "Overwriting $JBOSS_HOME/bin/run.conf" $LOG_FILE
cp $I2B2_INSTALL_HOME/config/jboss/run.conf $JBOSS_HOME/bin/
exit_if_bad $? "Could not overwrite $JBOSS_HOME/bin/run.conf" $LOG_FILE

#
# Overwrite the default server startup to set connectors to ports 9090 / 9009 ...
print_message "" $LOG_FILE
print_message "Overwriting $JBOSS_HOME/server/default/deploy/jboss-web.deployer/server.xml" $LOG_FILE
cp $I2B2_INSTALL_HOME/config/jboss/server.xml $JBOSS_HOME/server/default/deploy/jboss-web.deployer/
exit_if_bad $? "Could not overwrite $JBOSS_HOME/server/default/deploy/jboss-web.deployer/server.xml" $LOG_FILE

#
# Install apache axis2 v1.1...
print_message "" $LOG_FILE
print_message "Installing Apache axis2 v1.1 war file." $LOG_FILE
mkdir $JBOSS_HOME/server/default/deploy/i2b2.war
exit_if_bad $? "Could not create directory $JBOSS_HOME/server/default/deploy/i2b2.war" $LOG_FILE
unzip $WORK_DIR/$ACQUISITIONS_DIRECTORY/$AXIS_WAR -d $JBOSS_HOME/server/default/deploy/i2b2.war >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to unzip war file to $JBOSS_HOME/server/default/deploy/i2b2.war" $LOG_FILE

#=========================================================================
# If we got this far, we must be successful...
#=========================================================================
print_message "JBoss tailoring completed." $LOG_FILE
print_footer "2-tailor-jboss.sh" $JOB_NAME $LOG_FILE