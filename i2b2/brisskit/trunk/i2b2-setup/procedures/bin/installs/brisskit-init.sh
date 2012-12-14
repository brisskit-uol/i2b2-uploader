#!/bin/bash
#
# Initial script to load the brisskit functions. 
#
#

defaultinstalls=/installs
defaultlocal=/local
defaultlog=/local/log

set +o nounset
export brisskitinstalls=${brisskitinstalls:=${defaultinstalls}}
export brisskitlocal=${brisskitlocal:=${defaultlocal}}
export brisskitlog=${brisskitlog:=${defaultlog}}
set -o nounset


#
# Initial system setup.
brisskitinit() {
    infolog "brisskit system configuration"
    #

    installpackage unzip
    installpackage apache2
    installpackage php5
    installpackage libapache2-mod-php5
    installpackage php5-curl

    local webpath=http://private/artefacts
    

    cd /
    mkdir "${brisskitlocal}"
    wget  "${webpath}/java/jdk-6u30-linux-x64.bin" -P "${defaultlocal}"
    cd ${brisskitlocal}
    chmod -R 777 jdk-6u30-linux-x64.bin
    ./jdk-6u30-linux-x64.bin
    ln -s jdk1.6.0_30/ jdk
    rm jdk-6u30-linux-x64.bin

    wget  "${webpath}/ant/apache-ant-1.8.2-bin.zip" -P "${defaultlocal}"
    cd ${brisskitlocal}
    unzip apache-ant-1.8.2-bin.zip
    ln -s apache-ant-1.8.2/ ant
    rm apache-ant-1.8.2-bin.zip

    wget  "${webpath}/jboss/jboss-4.2.3.GA-brisskit-development.zip" -P "${defaultlocal}"
    cd ${brisskitlocal}
    unzip jboss-4.2.3.GA-brisskit-development.zip
    ln -s jboss-4.2.3.GA-brisskit-development/ jboss
    rm jboss-4.2.3.GA-brisskit-development.zip

    wget  "${webpath}/i2b2/server/i2b2-install-2.0-development.zip" -P "${defaultlocal}"   
    cd ${brisskitlocal}
    unzip i2b2-install-2.0-development.zip
    rm i2b2-install-2.0-development.zip

    wget  "${webpath}/i2b2/server/i2b2-procedures-2.0-development.zip" -P "${defaultlocal}"
    cd ${brisskitlocal}
    unzip i2b2-procedures-2.0-development.zip
    rm i2b2-procedures-2.0-development.zip

    cd ${brisskitlocal}
    wget "${webpath}/i2b2/server/setpaths.sh"
        
    cd ${brisskitlocal}

    echo "#!/bin/bash" > setpaths.sh
    echo "I2B2_INSTALL_HOME=${defaultlocal}/i2b2-install-2.0-development/" >> setpaths.sh
    echo "JAVA_HOME=${defaultlocal}/jdk/" >> setpaths.sh
    echo "ANT_HOME=${defaultlocal}/ant/" >> setpaths.sh
    echo "I2B2_PROCEDURES_HOME=${defaultlocal}/i2b2-procedures-2.0-development/" >> setpaths.sh

    echo "export I2B2_INSTALL_HOME" >> setpaths.sh
    echo "export JAVA_HOME" >> setpaths.sh
    echo "export ANT_HOME" >> setpaths.sh
    echo "export JBOSS_HOME" >> setpaths.sh
    echo "export I2B2_PROCEDURES_HOME" >> setpaths.sh

    set +o nounset
    I2B2_INSTALL_HOME=${defaultlocal}/i2b2-install-2.0-development/
    export I2B2_INSTALL_HOME
    I2B2_PROCEDURES_HOME=${defaultlocal}/i2b2-procedures-2.0-development/
    JAVA_HOME=${defaultlocal}/jdk/
    export JAVA_HOME
    ANT_HOME=${defaultlocal}/ant/
    export ANT_HOME
    JBOSS_HOME=${defaultlocal}/jboss/
    export JBOSS_HOME
    set -o nounset

    sudo apache2ctl restart    

    cd /i2b2-setup-2.0-development/bin/installs

}

#
# Check if a package is installed.
# Note: without --all deb will generate an error message.
# Note: with --all deb has to process a lot more and is slow.
installpackage()
    {
    local package=$1
    infolog "Checking system package [${package}]"
    local version=$(deb --query --all --queryformat '%{VERSION}' "${package}" 2> /dev/null)
    if [ -n "${version}" ]
    then
        infolog "Package [${package}][${version}] is installed"
    else
        infolog "Installing [${package}]"
        apt-get -y --force-yes install ${package}
    fi
    }

#
# Info logging.
infolog() {
    if [ -e "${brisskitlog}" ]
    then
        echo "INFO  : ${1}" | tee --append "${brisskitlog}/brisskit.log"
    else
        echo "INFO  : ${1}"
    fi
    }


brisskitinit

