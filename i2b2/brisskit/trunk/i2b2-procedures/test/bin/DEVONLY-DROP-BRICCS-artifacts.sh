#!/bin/bash
#-----------------------------------------------------------------------------------------------
# DROPS ALL SQL ARTIFACTS FOR THE BRICCS PROJECT
#  
# This means it DROPS ALL SQL ARTIFACTS FOR THE BRICCS PROJECT!!!
# For use in development only.
#
# Mandatory: 
#   (1) The I2B2_PROCEDURES_HOME environment variable to be set.
#   (2) This script and associated files can only have been generated using the
#       development profile within a maven development build.
#       It SHOULD NOT BE POSSIBLE for this to be present in a production build.
#
# USAGE: DEVONLY-DROP-BRICCS-artifacts.sh please drop everything
# Where:
#    'please drop everything' is a simple safety barrier.
#
# Author: Jeff Lusted (jl99@leicester.ac.uk)
#-----------------------------------------------------------------------------------------------
source $I2B2_PROCEDURES_HOME/bin/common/functions.sh
source $I2B2_PROCEDURES_HOME/bin/common/setenv.sh

print_project_install_usage() {
   echo " USAGE: DEVONLY-DROP-BRICCS-artifacts.sh please drop everything"
   echo " Where:"
   echo "   'please drop everything' is a simple safety barrier."
}

#=======================================================================
# First, some basic checks...
#=======================================================================
#
# Check on the usage...
if [ ! $# -eq 3 ]
then
	echo "Error! Incorrect number of arguments."
	echo ""
	print_project_install_usage
	exit 1
fi

if [ "$1" != please -o "$2" != drop -o "$3" != everything ] 
then 
	echo "I will not drop everything unless you ask correctly!"
	echo "Goodbye."
	echo ""
	print_project_install_usage
	exit 1
fi

#======================================================================
#   Make sure the user has every opportunity to back out...
#======================================================================
echo "Please confirm you wish to drop everything: (yes/no) ?"
read answer

if [ "$answer" != yes ]
then
	echo "Drop not confirmed. Goodbye."
	exit 1
fi
	
echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
echo "!!!!!!!   In 10 seconds all SQL artifacts for BRICCS will be dropped    !!!!!!!!"
echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
echo ""
echo "To abort, press Ctrl C"
echo ""
sleep 1
echo "10"
sleep 1
echo "9"
sleep 1
echo "8"
sleep 1
echo "7"
sleep 1
echo "6"
sleep 1
echo "5"
sleep 1
echo "4"
sleep 1
echo "3"
sleep 1
echo "2"
sleep 1
echo "1"	
sleep 1 

#===========================================================================
# The real work starts...
#===========================================================================  

#================================================
# Drop the data for project 'briccs'
#================================================
print_message ""
print_message "About to drop the crc data SQL artifacts for 'briccs' project." 
                  
$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -f $I2B2_PROCEDURES_HOME/ant/${DB_TYPE}/DEVONLY-DROP-BRICCS-artifacts.xml \
                  drop_briccs_data                 
exit_if_bad $? "Failed to drop crc data SQL artifacts for 'briccs' project." 
print_message "Success! Dropped crc data SQL artifacts for 'briccs' project."


#================================================
# Drop the metadata for project 'briccs'
#================================================
print_message ""
print_message "About to drop the metadata SQL artifacts for 'briccs' project." 
                  
$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -f $I2B2_PROCEDURES_HOME/ant/${DB_TYPE}/DEVONLY-DROP-BRICCS-artifacts.xml \
                  drop_briccs_metadata                
exit_if_bad $? "Failed to drop metadata SQL artifacts for 'briccs' project." 
print_message "Success! Dropped metadata SQL artifacts for 'briccs' project." 

#================================================
# Drop the workdata for project 'briccs'
#================================================
print_message ""
print_message "About to drop the workdata SQL artifacts for 'briccs' project." 
                  
$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -f $I2B2_PROCEDURES_HOME/ant/${DB_TYPE}/DEVONLY-DROP-BRICCS-artifacts.xml \
                  drop_briccs_workdata                
exit_if_bad $? "Failed to drop workdata SQL artifacts for 'briccs' project." 
print_message "Success! Dropped workdata SQL artifacts for 'briccs' project." 

#=========================================================================
# If we got this far, we must be successful (hopefully) ...
#=========================================================================
print_message "Goodbye."