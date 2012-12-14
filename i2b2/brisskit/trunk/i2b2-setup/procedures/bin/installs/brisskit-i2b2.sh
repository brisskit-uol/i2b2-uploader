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

cd $I2B2_INSTALL_HOME/bin/installs

./1-acquisitions.sh ${workspace}
./2-tailor-jboss.sh ${workspace}
./3-data-install.sh ${workspace}
./4-pm-install.sh ${workspace}
./5-ont-install.sh ${workspace}
./6-crc-install.sh ${workspace}
./7-work-install.sh ${workspace}
./8-fr-install.sh ${workspace}

mkdir "${brisskitlocal}/testpdo"
mkdir "${brisskitlocal}/logs"

chmod -R a+w "${brisskitlocal}/testpdo"
chmod -R a+w "${brisskitlocal}/logs"
chmod -R a+x /local/i2b2-procedures-2.0-development/bin/apps/catissue/
chmod -R a+x /local/i2b2-procedures-2.0-development/bin/apps/onyx/
#chmod -R a+x /local/i2b2-procedures-2.0-development/ontologies
#chmod -R a+x /local/i2b2-procedures-2.0-development/work/catissue
#chmod -R a+x /local/i2b2-procedures-2.0-development/work/first
chmod -R a+x /local/FRC

cd /var/www
mkdir i2b2
mv admin i2b2
cd i2b2
cp -r admin main
cd main
#sed 's/adminOnly: true,\r//' i2b2_config_data.js
sed 's/adminOnly: true,\r//' i2b2_config_data.js > i2b2_config_datax.js
cp i2b2_config_datax.js i2b2_config_data.js
rm i2b2_config_datax.js

}

brisskitinit

