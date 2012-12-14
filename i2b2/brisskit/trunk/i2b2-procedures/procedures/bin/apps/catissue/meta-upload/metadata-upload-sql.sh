#!/bin/bash
#-----------------------------------------------------------------------------------------------
# Uploads metadata for an i2b2 project.
#
# NB: This procedure uploads metadata to the ontology cell AND to the ontology dimension
#
# Prerequisites: appropriate executions of the following scripts:
#   (1) xslt-refined-2ontcell.sh
#   (2) xslt-refined-enum2ontcell.sh
#   (3) xslt-refined-2ontdim.sh
#   (4) xslt-refined-enum2ontdim.sh
#   (5) project-install.sh
#
# Mandatory: the I2B2_PROCEDURES_HOME environment variable to be set.
# Optional : the I2B2_PROCEDURES_WORKSPACE environment variable.
# The latter is an optional full path to a workspace area. If not set, defaults to a workspace
# within the procedures' home.
#
# Prerequisites: Settings within the directory I2B2_PROCEDURES_HOME/config/DB_TYPE 
# You are advised to review these settings carefully.
#
# USAGE: metadata-upload-sql.sh job-name
# Where: 
#   job-name is a suitable tag that groups all jobs associated with the overall workflow
# Notes:
#   The job-name is used to find the working directory for the overall workflow; eg:
#   I2B2_PROCEDURES_WORKSPACE/{job-name}
#   This working directory must already exist. 
#   It should be the working directory associated with creating the ontology sql.
#
# Further tailoring can be achieved via the defaults.sh script.
#
# Author: Jeff Lusted (jl99@leicester.ac.uk)
#-----------------------------------------------------------------------------------------------
source $I2B2_PROCEDURES_HOME/bin/common/functions.sh
source $I2B2_PROCEDURES_HOME/bin/common/setenv.sh

print_metadata_upload_sql_usage() {
   echo " USAGE: metadata-upload-sql.sh job-name"
   echo " Where: "
   echo "   job-name is a suitable tag that groups all jobs associated with the overall workflow"
   echo " Notes:"
   echo "   The job-name is used to find the working directory for the overall workflow; eg:"
   echo "   I2B2_PROCEDURES_WORKSPACE/{job-name}"
   echo "   This working directory must already exist. "
   echo "   It should be the working directory associated with creating the ontology sql."
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
	print_metadata_upload_sql_usage
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
print_banner "metadata-upload-sql" $JOB_NAME $LOG_FILE 

#===========================================================================
# The real work is about to start.
# Give the user a warning...
#=========================================================================== 
print_message "About to upload project metadata" $LOG_FILE
echo "This should take some minutes."
echo ""
echo "Detailed log messages are written to $LOG_FILE"
echo "If you want to see this during execution, try: tail -f $LOG_FILE"
echo ""

P_METADATA_SQL_DIRECTORY=e-ontology-sql

#===========================================================================
# UPLOAD METADATA TO ONTOLOGY CELL...
#===========================================================================
$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -Dsql.dir=$WORK_DIR/$P_METADATA_SQL_DIRECTORY \
                  -f $I2B2_PROCEDURES_HOME/ant/${DB_TYPE}/metadata-upload-build.xml \
                  load_ontcell_metadata \
                  >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to upload metadata to ontology cell." $LOG_FILE
print_message "Success! Metadata uploaded to ontology cell." $LOG_FILE

#===========================================================================
# UPLOAD METADATA TO ONTOLOGY DIMENSION...
#===========================================================================
$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -Dsql.dir=$WORK_DIR/$P_METADATA_SQL_DIRECTORY \
                  -f $I2B2_PROCEDURES_HOME/ant/${DB_TYPE}/metadata-upload-build.xml \
                  load_ontdim_metadata \
                  >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to upload metadata to ontology dimension." $LOG_FILE
print_message "Success! Metadata uploaded to ontology dimension." $LOG_FILE

#=========================================================================
# If we got this far, we must be successful...
#=========================================================================
echo "Please check the log messages for SQL errors."
print_footer "metadata-upload-sql" $JOB_NAME $LOG_FILE
