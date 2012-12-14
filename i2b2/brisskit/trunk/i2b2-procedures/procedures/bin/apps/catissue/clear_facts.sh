cd /local
source setpaths.sh

source $I2B2_PROCEDURES_HOME/bin/common/functions.sh
source $I2B2_PROCEDURES_HOME/bin/common/setenv.sh

#
# Clear file repository cell...
echo "0"
rm -Rf /local/FRC/*

#
# Clear or create work directories...
echo "1"
if [ ! -d $I2B2_PROCEDURES_HOME/work/catissue ]
then
        mkdir $I2B2_PROCEDURES_HOME/work/catissue
        mkdir $I2B2_PROCEDURES_HOME/work/catissue/f-pdo
        mkdir $I2B2_PROCEDURES_HOME/work/catissue/e-ontology-catissue-sql
        mkdir $I2B2_PROCEDURES_HOME/work/catissue/d-refined-metadata-enums-catissue
        mkdir $I2B2_PROCEDURES_HOME/work/catissue/c-refined-metadata-catissue
else
        rm -Rf $I2B2_PROCEDURES_HOME/work/catissue/f-pdo/
        rm -Rf $I2B2_PROCEDURES_HOME/work/catissue/e-ontology-catissue-sql/
        rm -Rf $I2B2_PROCEDURES_HOME/work/catissue/d-refined-metadata-enums-catissue/
        rm -Rf $I2B2_PROCEDURES_HOME/work/catissue/c-refined-metadata-catissue/

fi

echo "2"
$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -f $I2B2_PROCEDURES_HOME/ant/${DB_TYPE}/catissue_db.xml clear_facts
echo "3"

$ANT_HOME/bin/ant -propertyfile $I2B2_PROCEDURES_HOME/config/config.properties \
                  -Dproc.home=$I2B2_PROCEDURES_HOME \
                  -f $I2B2_PROCEDURES_HOME/ant/${DB_TYPE}/catissue_db.xml clear_metadata
echo "4"
