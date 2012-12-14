#!/bin/bash
#------------------------------------------------------------------------------------------------------------
# Takes the enumerated refined metadata files (> 600 of them) and produces SQL inserts for the ontology cell
# Currently Oracle based.
#
# Prerequisites: The refine-2metadata and the xslt-refined-2ontcell scripts must have been successfully run.
#
# Mandatory: the following environment variables must be set
#            I2B2_PROCEDURES_HOME, JAVA_HOME           
# Optional : the I2B2_PROCEDURES_WORKSPACE environment variable.
# The latter is an optional full path to a workspace area. If not set, defaults to a workspace
# within the procedures' home.
#
# USAGE: 8-xslt-refined-enum2ontcell job-name 
# Where: 
#   job-name is a suitable tag that groups all jobs associated within the overall workflow
# Notes:
#   The job-name must be associated with the prerequisite run of the refine-metadata script.
#
# Author: Jeff Lusted (jl99@leicester.ac.uk)
#-------------------------------------------------------------------------------------------------------------
source $I2B2_PROCEDURES_HOME/bin/common/functions.sh
source $I2B2_PROCEDURES_HOME/bin/common/setenv.sh

print_xslt_refined_enum2ontcell_usage() {
   echo " USAGE: 8-xslt-refined-enum2ontcell.sh job-name"
   echo " Where:"
   echo "   job-name is a suitable tag that groups all jobs associated with the overall workflow"
   echo " Notes:"
   echo "   The job-name must be associated with the prerequisite run of the refine-metadata script."
}

#REFINED_METADATA_CATISSUE_DIRECTORY
#METADATA_CATISSUE_SQL_DIRECTORY
#ont_cell_catissue_${DB_TYPE}.xsl
#$REFINED_METADATA_CATISSUE_ENUMS_DIRECTORY

P_REFINED_METADATA_DIRECTORY=c-refined-metadata
P_METADATA_SQL_DIRECTORY=e-ontology-sql
P_ONT_CELL=ont_cell_generic
P_REFINED_METADATA_ENUMS_DIRECTORY=d-refined-metadata-enums



#=======================================================================
# First, some basic checks...
#=======================================================================
#
# Check on the usage...
if [ ! $# -eq 1 ]
then
	echo "Error! Incorrect number of arguments"
	echo ""
	print_xslt_refined_enum2ontcell_usage
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
# The input directory is...
INPUT_DIR=$I2B2_PROCEDURES_WORKSPACE/$JOB_NAME/$P_REFINED_METADATA_ENUMS_DIRECTORY
#
# And it must exist!
if [ ! -d $INPUT_DIR ]
then
	echo "Error! Input directory does not exist: $INPUT_DIR"
	echo "Please check your job name: $JOB_NAME. Exiting..."
	exit 1
fi

#
# We use the log file for the job...
LOG_FILE=$I2B2_PROCEDURES_WORKSPACE/$JOB_NAME/$JOB_LOG_NAME

#===========================================================================
# Print a banner for this step of the job.
#===========================================================================
print_banner "xslt-refined-enum2ontcell" $JOB_NAME $LOG_FILE

#
# And the output directory is...
OUTPUT_DIR=$I2B2_PROCEDURES_WORKSPACE/$JOB_NAME/$P_METADATA_SQL_DIRECTORY/$DB_TYPE/ontcell
#
# And it must exist!
if [ ! -d $OUTPUT_DIR ]
then
	print_message "Output directory does not exist: $OUTPUT_DIR" $LOG_FILE
	print_message "Please check your job name: $JOB_NAME"
	exit 1
fi

#
# Here is the stylesheet...
STYLESHEET=$I2B2_PROCEDURES_HOME/xslt/$DB_TYPE/${P_ONT_CELL}_enumerations_${DB_TYPE}.xsl

#===========================================================================
# The real work is about to start.
# Give the user a warning...
#===========================================================================
print_message "About to produce enumerated concepts SQL for the ontology cell" $LOG_FILE
echo ""
echo "Detailed log messages (if any) are written to $LOG_FILE"
echo "If you want to see this during execution, try: tail -f $LOG_FILE"
echo ""
#
# Do the business...
print_message "" $LOG_FILE
print_message "Applying style sheet transformation to enumerations..." $LOG_FILE
$JAVA_HOME/bin/java \
           -cp $(for i in $I2B2_PROCEDURES_HOME/lib/*.jar ; do echo -n $i: ; done). \
           net.sf.saxon.Transform \
           -xsl:$STYLESHEET \
           -s:$INPUT_DIR \
           -o:$OUTPUT_DIR \
           adminDate=`date +%Y-%m-%dT%H:%M:%S.000%:z` \
           sourceSystem=BRICCS >>$LOG_FILE 2>>$LOG_FILE
exit_if_bad $? "Failed to produce enumerated concepts SQL for the ontology cell." $LOG_FILE
print_message "Success! XSLT transformation complete." $LOG_FILE

#
# Rename the files from extension *.xml to *.sql ... 
print_message "" $LOG_FILE
print_message "Giving files the sql extension..." $LOG_FILE
for filename in $OUTPUT_DIR/*.xml
do
	w_o_ext=`basename $filename .xml`;
	mv $filename $OUTPUT_DIR/$w_o_ext.sql ;
	exit_if_bad $? "Failed to give a file the sql extension." $LOG_FILE
done
print_message "Success! All files now have a suitable sql extension name." $LOG_FILE
     
#
# If we got this far, we must be successful...
print_footer "xslt-refined-enum2ontcell" $JOB_NAME $LOG_FILE



 
