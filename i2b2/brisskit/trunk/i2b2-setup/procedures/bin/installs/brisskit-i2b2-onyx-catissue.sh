#!/bin/bash
#
# Ensure default and config are setup correctly. 
#
#

defaultinstalls=/installs
defaultlocal=/local
defaultlog=/local/log
workspace=first

set +o nounset
export brisskitinstalls=${brisskitinstalls:=${defaultinstalls}}
export brisskitlocal=${brisskitlocal:=${defaultlocal}}
export brisskitlog=${brisskitlog:=${defaultlog}}
set -o nounset

#
# Initial system setup.
brisskitinit() {

# edit /local/i2b2-procedures-2.0-development/bin/apps/catissue/database/ant/scripts/create_catissue.sql
# make sure the 3 databases are in it. 
# cp config.properties /local/i2b2-procedures-2.0-development/config
# cp defaults.sh /local/i2b2-procedures-2.0-development/config

cd $I2B2_PROCEDURES_HOME/bin/project-install 
./1-project-install-onyx-and-catissue.sh ${workspace}
./2-update-datasources.sh ${workspace}

# catissue ontology
cd $I2B2_PROCEDURES_HOME/ontologies/catissue/ontology
mkdir $I2B2_PROCEDURES_HOME/work/${workspace}/c-refined-metadata-catissue
cp -r * $I2B2_PROCEDURES_HOME/work/${workspace}/c-refined-metadata-catissue

# catissue enums
mkdir $I2B2_PROCEDURES_HOME/work/${workspace}/d-refined-metadata-enums-catissue

# onyx ontology
cd $I2B2_PROCEDURES_HOME/ontologies/onyx/ontology
mkdir $I2B2_PROCEDURES_HOME/work/${workspace}/c-refined-metadata
cp -r * $I2B2_PROCEDURES_HOME/work/${workspace}/c-refined-metadata

#onyx enums
cd $I2B2_PROCEDURES_HOME/ontologies/onyx/ontology-enums
mkdir $I2B2_PROCEDURES_HOME/work/${workspace}/d-refined-metadata-enums
cp -r * $I2B2_PROCEDURES_HOME/work/${workspace}/d-refined-metadata-enums

# catissue upload ontology
cd $I2B2_PROCEDURES_HOME/bin/apps/catissue/ontology-prep
./6-xslt-refined-2ontcell-catissue.sh ${workspace}
./7-xslt-refined-2ontdim-catissue.sh ${workspace}

# catissue upload enum
# no enums first time round, leave commented
#./8-xslt-refined-enum2ontcell.sh ${workspace} 
#./9-xslt-refined-enum2ontdim.sh ${workspace}

# catissue upload
cd $I2B2_PROCEDURES_HOME/bin/apps/catissue/meta-upload 
./metadata-upload-sql.sh ${workspace}

# onyx upload ontology
cd $I2B2_PROCEDURES_HOME/bin/apps/onyx/ontology-prep
./6-xslt-refined-2ontcell.sh ${workspace}
./7-xslt-refined-2ontdim.sh ${workspace}

# onyx upload enum
./8-xslt-refined-enum2ontcell.sh ${workspace} 
./9-xslt-refined-enum2ontdim.sh ${workspace}

# onyx upload
cd $I2B2_PROCEDURES_HOME/bin/apps/onyx/meta-upload 
./metadata-upload-sql.sh ${workspace}


}

brisskitinit
