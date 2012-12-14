#!/bin/bash

echo "starting catissue file"

cd /local

source setpaths.sh

echo $JAVA_HOME

cd /local/i2b2-procedures-2.0-development/bin/apps/catissue/

./clear_facts.sh

ssh integration@catissue /var/local/brisskit/integration/catissue_i2b2/caTissue_i2b2.sh
#ssh ss727@catissue 'bash -s' < catissue_i2b2/caTissue_i2b2.sh

#cd catissue_i2b2

#./caTissue_i2b2.sh

# move files from caTissue

#exit

cd /local/i2b2-procedures-2.0-development/bin/apps/catissue/

./import.sh
