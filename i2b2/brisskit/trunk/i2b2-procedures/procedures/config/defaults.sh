#!/bin/bash
#
# Default settings used by scripts within the bin directory
# 
#-------------------------------------------------------------------

# Log file name:
JOB_LOG_NAME=job.log

# Name of directory where onyx export file will be unzipped:
ONYX_EXPORT_DIRECTORY=a-onyx-export

# Name of directory to hold first evolution of metadata files:
METADATA_DIRECTORY=b-metadata

# Name of directory to hold second evolution of main metadata file:
REFINED_METADATA_DIRECTORY=c-refined-metadata

# Name of directory to hold second evolution of main metadata enumeration files:
REFINED_METADATA_ENUMS_DIRECTORY=d-refined-metadata-enums

# Name of directory to hold sql inserts for metadata tables:
METADATA_SQL_DIRECTORY=e-ontology-sql

# Name of directory to hold generated PDO xml files:
PDO_DIRECTORY=f-pdo

# Name of directory to hold SQL inserts derived from PDO xml files:
PDO_SQL_DIRECTORY=g-pdo-sql

# Max number of participants to be folded into one PDO xml file:
BATCH_SIZE=50

# Name of file that holds the central metadata details:
MAIN_REFINED_METADATA_FILE_NAME=refined-metadata.xml

# Name of the DB; can be either 'oracle' or 'sqlserver'
DB_TYPE=sqlserver

# Name of directory to hold any zipped SQL test data (PDO based)
ZIPPED_SQL_DIRECTORY=z-zipped-sql

# Catissue
REFINED_METADATA_CATISSUE_DIRECTORY=c-refined-metadata-catissue
REFINED_METADATA_CATISSUE_ENUMS_DIRECTORY=d-refined-metadata-enums-catissue
METADATA_CATISSUE_SQL_DIRECTORY=e-ontology-catissue-sql
MAIN_REFINED_METADATA_CATISSUE_FILE_NAME=caTissue-Refined-Metadata8.xml

# Custom space for the install workspace (if required)
# If not defined, defaults to I2B2_INSTALL_HOME/work
#I2B2_PROCEDURES_WORKSPACE=?

# Custom names for the projects JBoss dataset config files.
# These names MUST be unique within a JBoss deployment.
# They MUST end with the string '-ds.xml'
# Suggestions:
# If your project is named 'briccs1' then the following would work:
# briccs1-crc-ds.xml
# briccs1-ont-ds.xml
# briccs1-work-ds.xml
#
# The recommendation (not strict) is that these should be the same 
# as the project.name setting within the config.properties file
#
CRC_DS=REPLACEME-crc-ds.xml
ONT_DS=REPLACEME-ont-ds.xml
WORK_DS=REPLACEME-work-ds.xml
