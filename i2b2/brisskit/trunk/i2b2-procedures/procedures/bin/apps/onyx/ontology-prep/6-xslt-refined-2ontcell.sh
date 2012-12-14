#!/bin/bash
#-----------------------------------------------------------------------------------------------
# Takes the main refined metadata file and produces SQL inserts for the ontology cell.
#
# Currently Oracle based.
# The main input file is named RefinedMetadata.xml.
#
# Prerequisite: The refine-2metadata script must have been successfully run.
#
# Mandatory: the following environment variables must be set
#            I2B2_PROCEDURES_HOME, JAVA_HOME            
# Optional : the I2B2_PROCEDURES_WORKSPACE environment variable.
# The latter is an optional full path to a workspace area. If not set, defaults to a workspace
# within the procedures' home.
#
# USAGE: 6-xslt-refined-2ontcell job-name 
# Where: 
#   job-name is a suitable tag that groups all jobs associated within the overall workflow
# Notes:
#   The job-name must be associated with the prerequisite run of the refine-metadata script.
#
# Author: Jeff Lusted (jl99@leicester.ac.uk)
#-----------------------------------------------------------------------------------------------
source $I2B2_PROCEDURES_HOME/bin/common/functions.sh
source $I2B2_PROCEDURES_HOME/bin/common/setenv.sh

print_xslt_refined_2ontcell_usage() {
   echo " USAGE: 6-xslt-refined-2ontcell.sh job-name"
   echo " Where:"
   echo "   job-name is a suitable tag that groups all jobs associated with the overall workflow"
   echo " Notes:"
   echo "   The job-name must be associated with the prerequisite run of the refine-metadata script."
}

#=======================================================================
# First, some basic checks...
#=======================================================================
#
# Check on the usage...
if [ ! $# -eq 1 ]
then
	echo "Error! Incorrect number of arguments"
	echo ""
	print_xslt_refined_2ontcell_usage
	exit 1
fi

#
# Retrieve the argument into its variable...
JOB_NAME=$1

#
# It is possible to set your own procedures workspace.
# But if it doesn't exist, we create one for you within the procedures home...
if [ -z $I2B2_PROCEDURES_WORKSPACE ]
then
	I2B2_PROCEDURES_WORKSPACE=$I2B2_PROCEDURES_HOME/work
fi

#
# We use the log file for the job...
LOG_FILE=$I2B2_PROCEDURES_WORKSPACE/$JOB_NAME/$JOB_LOG_NAME

#
# The input directory is...
INPUT_DIR=$I2B2_PROCEDURES_WORKSPACE/$JOB_NAME/$REFINED_METADATA_DIRECTORY
#
# And it must exist!
if [ ! -d $INPUT_DIR ]
then
	echo "Error! Input directory does not exist: $INPUT_DIR"
	echo "Please check your job name: $JOB_NAME. Exiting..."
	exit 1
fi

#===========================================================================
# Print a banner for this step of the job.
#===========================================================================
print_banner "xslt-refined-2ontcell" $JOB_NAME $LOG_FILE 

#
# And the output directory is...
OUTPUT_DIR=$I2B2_PROCEDURES_WORKSPACE/$JOB_NAME/$METADATA_SQL_DIRECTORY/$DB_TYPE/ontcell
#
# And it must NOT exist!
if [ -d $OUTPUT_DIR ]
then
	print_message "Error! Output directory exists: $OUTPUT_DIR. Exiting..."
	exit 1
fi

#
# Make the output directory...
mkdir -p $OUTPUT_DIR
exit_if_bad $? "Failed to create output directory. $OUTPUT_DIR"

#
# Here is the stylesheet...
STYLESHEET=$I2B2_PROCEDURES_HOME/xslt/$DB_TYPE/ont_cell_${DB_TYPE}.xsl

#===========================================================================
# The real work is about to start.
# And give the user a warning...
#===========================================================================
print_message "About to produce SQL for the ontology cell"
echo "This should take under 60 seconds."
echo ""
echo "Detailed log messages are written to $LOG_FILE"
echo "If you want to see this during execution, try: tail -f $LOG_FILE"
echo ""
sleep 5
#
# Do the business...
$JAVA_HOME/bin/java \
        -cp $(for i in $I2B2_PROCEDURES_HOME/lib/*.jar ; do echo -n $i: ; done). \
         net.sf.saxon.Transform \
         -xsl:$STYLESHEET \
         -s:$INPUT_DIR/$MAIN_REFINED_METADATA_FILE_NAME \
         -o:$OUTPUT_DIR/1-OntologyCell.sql \
         adminDate=`date +%Y-%m-%dT%H:%M:%S.000%:z` \
         sourceSystem=BRICCS >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to produce SQL for the ontology cell."         
#
# If we got this far, we must be successful...
print_footer "xslt-refined-2ontcell" $JOB_NAME $LOG_FILE