#!/bin/bash
#-----------------------------------------------------------------------------------------------
# Installs an i2b2 project but DOES NOT upload meta data or participant data.
#
# Mandatory: the I2B2_PROCEDURES_HOME environment variable to be set.
# Optional : the I2B2_PROCEDURES_WORKSPACE environment variable.
# The latter is an optional full path to a workspace area. If not set, defaults to a workspace
# within the procedures' home.
#
# Prerequisites: Settings within the directory I2B2_PROCEDURES_HOME/config/DB_TYPE 
# You are advised to review these settings carefully.
#
# USAGE: 1-project-install job-name
# Where: 
#   job-name is a suitable tag to group all jobs associated with the overall workflow
# Notes:
#   The job-name is used to create a working directory for the overall workflow; eg:
#   I2B2_PROCEDURES_WORKSPACE/job-name
#   This working directory is created if it does not exist.
#
# Further tailoring can be achieved via the defaults.sh script.
#
# Author: Jeff Lusted (jl99@leicester.ac.uk)
#-----------------------------------------------------------------------------------------------
source $I2B2_PROCEDURES_HOME/bin/common/functions.sh
source $I2B2_PROCEDURES_HOME/bin/common/setenv.sh

print_project_install_usage() {
   echo " USAGE: 1-project-install.sh job-name"
   echo " Where:"
   echo "   job-name is a suitable tag to group all jobs associated with the overall workflow"
   echo " Notes:"
   echo "   The job-name is used to create a working directory for the overall workflow; eg:"
   echo "   I2B2_PROCEDURES_WORKSPACE/job-name"
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
if [ -z $I2B2_PROCEDURES_WORKSPACE ]
then
	I2B2_PROCEDURES_WORKSPACE=$I2B2_PROCEDURES_HOME/work
fi

#
# Establish a log file for the job...
LOG_FILE=$I2B2_PROCEDURES_WORKSPACE/$JOB_NAME/$JOB_LOG_NAME

#
# If required, create a working directory for this job step.
WORK_DIR=$I2B2_PROCEDURES_WORKSPACE/$JOB_NAME
if [ ! -d $WORK_DIR ]
then
	mkdir -p $WORK_DIR
	exit_if_bad $? "Failed to create working directory. $WORK_DIR"
fi

#===========================================================================
# Print a banner for this step of the job.
#===========================================================================
print_banner "project-install" $JOB_NAME $LOG_FILE 

#===========================================================================
# The real work is about to start.
# Give the user a warning...
#=========================================================================== 
print_message "About to install an i2b2 project" $LOG_FILE
echo "This should take about a minute."
echo ""
echo "Detailed log messages are written to $LOG_FILE"
echo "If you want to see this during execution, try: tail -f $LOG_FILE"
echo ""

#===========================================================================
# METADATA CREATE AND STATIC LOAD: CUSTOMER WITH ONYX ONLY
#===========================================================================
#
# Create tables, indexes and sequences...
$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -f $I2B2_PROCEDURES_HOME/ant/$DB_TYPE/project-install-build.xml \
                  create_onyx_metadata_tables \
                  >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to create metadata tables, indexes and sequences for project" $LOG_FILE
print_message "Success! Created metadata tables, indexes and sequences for project." $LOG_FILE

#
# Load the static metadata...
$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -f $I2B2_PROCEDURES_HOME/ant/$DB_TYPE/project-install-build.xml \
                  load_onyx_static_metadata \
                  >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to load project metadata." $LOG_FILE
print_message "Success! Static metadata for project loaded successfully." $LOG_FILE

#==========================================================================
# WORKDATA CREATE AND STATIC LOAD...
#==========================================================================
#
# Create workdata tables...
$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -f $I2B2_PROCEDURES_HOME/ant/$DB_TYPE/project-install-build.xml \
                  create_workdata_tables \
                  >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to create project workdata tables." $LOG_FILE
print_message "Success! Work data tables for project created successfully." $LOG_FILE

#
# Load the work data for project."
$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -f $I2B2_PROCEDURES_HOME/ant/$DB_TYPE/project-install-build.xml \
                  load_workdata \
                  >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to load project workdata." $LOG_FILE
print_message "Success! Work data for project loaded successfully." $LOG_FILE

#=========================================================================
# LOAD HIVE DATA FOR PROJECT...
#=========================================================================
#
# Copy hive SQL files to required location
# and merge required substitution variables into them...
merge_config_properties \
            $I2B2_PROCEDURES_HOME/config/config.properties \
            $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/hive/inserts/crc_db_lookup_${DB_TYPE}_insert_data.sql \
            $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/hive/inserts/crc_db_lookup_${DB_TYPE}_insert_data.sql.tmp         
exit_if_bad $? "Failed to merge properties into $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/hive/inserts/crc_db_lookup_${DB_TYPE}_insert_data.sql.tmp" $LOG_FILE  

merge_config_properties \
            $I2B2_PROCEDURES_HOME/config/config.properties \
            $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/hive/inserts/ont_db_lookup_${DB_TYPE}_insert_data.sql \
            $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/hive/inserts/ont_db_lookup_${DB_TYPE}_insert_data.sql.tmp         
exit_if_bad $? "Failed to merge properties into $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/hive/inserts/ont_db_lookup_${DB_TYPE}_insert_data.sql.tmp" $LOG_FILE  

merge_config_properties \
            $I2B2_PROCEDURES_HOME/config/config.properties \
            $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/hive/inserts/work_db_lookup_${DB_TYPE}_insert_data.sql \
            $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/hive/inserts/work_db_lookup_${DB_TYPE}_insert_data.sql.tmp         
exit_if_bad $? "Failed to merge properties into $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/hive/inserts/work_db_lookup_${DB_TYPE}_insert_data.sql.tmp" $LOG_FILE  

$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -f $I2B2_PROCEDURES_HOME/ant/${DB_TYPE}/project-install-build.xml \
                  load_hivedata \
                  >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to load project hivedata." $LOG_FILE
#
# Remove the temporary files...
rm -f $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/hive/inserts/*_db_lookup_${DB_TYPE}_insert_data.sql.tmp

print_message "Success! Hive data for project loaded successfully." $LOG_FILE

#=========================================================================
# LOAD PM DATA FOR PROJECT...
#=========================================================================
merge_config_properties \
            $I2B2_PROCEDURES_HOME/config/config.properties \
            $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/pm/inserts/pm_access_insert_data.sql \
            $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/pm/inserts/pm_access_insert_data.sql.tmp
exit_if_bad $? "Failed to merge properties into $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/pm/inserts/pm_access_insert_data.sql.tmp" $LOG_FILE 

$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -f $I2B2_PROCEDURES_HOME/ant/${DB_TYPE}/project-install-build.xml \
                  load_pmdata \
                  >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to load project pmdata." $LOG_FILE

#
# Remove the temporary files...
rm -f $I2B2_PROCEDURES_HOME/sql/${DB_TYPE}/pm/inserts/pm_access_insert_data.sql.tmp

print_message "Success! Pmdata for project loaded successfully." $LOG_FILE

#=========================================================================
# CRC CREATE AND STATIC LOAD...
#=========================================================================
#
# Create data tables, indexes and sequences for project
$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -f $I2B2_PROCEDURES_HOME/ant/${DB_TYPE}/project-install-build.xml \
                  create_crc_tables \
                  >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to create data tables, indexes and sequences for project" $LOG_FILE
echo "Success! Created data tables, indexes and sequences for project." $LOG_FILE

#
# Create stored procedures
$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -f $I2B2_PROCEDURES_HOME/ant/${DB_TYPE}/project-install-build.xml \
                  create_procedures \
                  >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to create stored procedures for project." $LOG_FILE
echo "Success! Created stored procedures for project." $LOG_FILE        

#=========================================================================
# If we got this far, we must be successful...
#=========================================================================
print_footer "project-install" $JOB_NAME $LOG_FILE