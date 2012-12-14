echo "0"

cd /local
source setpaths.sh

source $I2B2_PROCEDURES_HOME/bin/common/functions.sh
source $I2B2_PROCEDURES_HOME/bin/common/setenv.sh

#
# Clear or create work directories...
echo "1"
if [ ! -d $I2B2_PROCEDURES_HOME/work/onyx ]
then
	mkdir $I2B2_PROCEDURES_HOME/work/onyx
	mkdir $I2B2_PROCEDURES_HOME/work/onyx/e-ontology-catissue-sql
	mkdir $I2B2_PROCEDURES_HOME/work/onyx/d-refined-metadata-enums
else
	rm $I2B2_PROCEDURES_HOME/work/onyx/e-ontology-catissue-sql/*
	rm $I2B2_PROCEDURES_HOME/work/onyx/d-refined-metadata-enums/*
fi

echo "1"
cp $I2B2_PROCEDURES_HOME/ontologies/onyx/ontology/$MAIN_REFINED_METADATA_FILE_NAME \
   $I2B2_PROCEDURES_HOME/work/onyx/$REFINED_METADATA_DIRECTORY
   
echo "2"
cp $I2B2_PROCEDURES_HOME/ontologies/onyx/ontology-enums/* \
   $I2B2_PROCEDURES_HOME/work/onyx/$REFINED_METADATA_ENUMS_DIRECTORY

echo "3"
$I2B2_PROCEDURES_HOME/bin/apps/onyx/ontology-prep/6-xslt-refined-2ontcell-onyx.sh onyx 

echo "4"
$I2B2_PROCEDURES_HOME/bin/apps/onyx/ontology-prep/7-xslt-refined-2ontdim-onyx.sh  onyx 

echo "5"
$I2B2_PROCEDURES_HOME/bin/apps/onyx/ontology-prep/8-xslt-refined-enum2ontcell.sh onyx

echo "6"
$I2B2_PROCEDURES_HOME/bin/apps/onyx/ontology-prep/9-xslt-refined-enum2ontdim.sh onyx

echo "7"
$I2B2_PROCEDURES_HOME/bin/apps/onyx/meta-upload/metadata-upload-sql.sh onyx

echo "8"
