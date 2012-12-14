echo "0"

cd /local
source setpaths.sh

source $I2B2_PROCEDURES_HOME/bin/common/functions.sh
source $I2B2_PROCEDURES_HOME/bin/common/setenv.sh

echo "1"
mkdir $I2B2_PROCEDURES_HOME/work/catissue/$REFINED_METADATA_CATISSUE_DIRECTORY

cp $I2B2_PROCEDURES_HOME/ontologies/catissue/ontology/$MAIN_REFINED_METADATA_CATISSUE_FILE_NAME \
   $I2B2_PROCEDURES_HOME/work/catissue/$REFINED_METADATA_CATISSUE_DIRECTORY

mkdir $I2B2_PROCEDURES_HOME/work/catissue/$REFINED_METADATA_CATISSUE_ENUMS_DIRECTORY
echo "2"
cp $I2B2_PROCEDURES_HOME/ontologies/catissue/ontology-enums/* \
   $I2B2_PROCEDURES_HOME/work/catissue/$REFINED_METADATA_CATISSUE_ENUMS_DIRECTORY

echo "3"
$I2B2_PROCEDURES_HOME/bin/apps/catissue/ontology-prep/6-xslt-refined-2ontcell-catissue.sh catissue

echo "4"
$I2B2_PROCEDURES_HOME/bin/apps/catissue/ontology-prep/7-xslt-refined-2ontdim-catissue.sh  catissue

echo "5"
$I2B2_PROCEDURES_HOME/bin/apps/catissue/ontology-prep/8-xslt-refined-enum2ontcell.sh catissue

echo "6"
$I2B2_PROCEDURES_HOME/bin/apps/catissue/ontology-prep/9-xslt-refined-enum2ontdim.sh catissue

echo "7"
$I2B2_PROCEDURES_HOME/bin/apps/catissue/meta-upload/metadata-upload-sql.sh catissue

#??? Ensure PDO's do exist

#echo "8"
#$I2B2_PROCEDURES_HOME/bin/participant-upload/participant-upload-ws.sh catissue

echo "9"
