#!/bin/sh
#
#  pirsfload.sh
###########################################################################
#
#  Purpose:  This script controls the execution of the pirsfload.
#
#  Usage:
#
#      pirsfload.sh
#
#  Env Vars:
#
#      See the configuration file
#
#  Inputs:
#
#      - Common configuration file (common.config.sh)
#      - load configuration file (config)
#
#  Outputs:
#
#      - An archive file
#      - Log files defined by the environment variables ${LOG_PROC},
#        ${LOG_DIAG}, ${LOG_CUR} and ${LOG_VAL}
#      - BCP files for each database table to be loaded
#      - Records written to the database tables
#      - Exceptions written to standard error
#      - Configuration and initialization errors are written to a log file
#        for the shell script
#
#  Exit Codes:
#
#      0:  Successful completion
#      1:  Fatal error occurred
#      2:  Non-fatal error occurred
#
#  Assumes:  Nothing
#
#  Notes:  None
#
###########################################################################

#
#  Set up a log file for the shell script in case there is an error
#  during configuration and initialization.
#
cd `dirname $0`/..
LOG=`pwd`/pirsfload.log
rm -f ${LOG}

#
#  Verify the argument(s) to the shell script.
#
if [ $# -ne 0 ]
then
    echo "Usage: $0" | tee -a ${LOG}
    exit 1
fi

#
#  Establish the configuration file names.
#
COMMON_CONFIG=`pwd`/common.config.sh
CONFIG=`pwd`/pirsfload.config

#
#  Make sure the configuration files are readable.
#
if [ ! -r ${COMMON_CONFIG} ]
then
    echo "Cannot read configuration file: ${COMMON_CONFIG}" | tee -a ${LOG}
    exit 1
fi
if [ ! -r ${CONFIG} ]
then
    echo "Cannot read configuration file: ${CONFIG}" | tee -a ${LOG}
    exit 1
fi

#
#  Source the common configuration file.
#
. ${COMMON_CONFIG}

#
#  Source the common DLA functions script.
#
if [ "${DLAJOBSTREAMFUNC}" != "" ]
then
    if [ -r ${DLAJOBSTREAMFUNC} ]
    then
        . ${DLAJOBSTREAMFUNC}
    else
        echo "Cannot source DLA functions script: ${DLAJOBSTREAMFUNC}" | tee -a ${LOG}
        exit 1
    fi
else
    echo "Environment variable DLAJOBSTREAMFUNC has not been defined." | tee -a ${LOG}
    exit 1
fi

#
#  Source the load configuration file.
#
. ${CONFIG}

#
#  Perform pre-load tasks.
#
preload

#
#  Run the load application.
#
echo "\n`date`" >> ${LOG_PROC}
echo "Run the PIRSFLoad application" >> ${LOG_PROC}
/usr/local/bin/gunzip -c ${INPUT_FILENAME}|${JAVA} ${JAVARUNTIMEOPTS} -classpath ${CLASSPATH} \
        -DCONFIG=${COMMON_CONFIG},${CONFIG} \
        -DJOBKEY=${JOBKEY} ${DLA_START}
STAT=$?
if [ ${STAT} -ne 0 ]
then
    echo "PIRSFLoad application failed.  Return status: ${STAT}" >> ${LOG_PROC}
    postload
    exit 1
fi

#
# run qc reports
#
${APP_QCRPT} ${RPTDIR} ${MGD_DBSERVER} ${MGD_DBNAME} ${RADAR_DBNAME} ${JOBKEY}
STAT=$?
if [ ${STAT} -ne 0 ]
then
    echo "Running seqloader QC reports failed.	Return status: ${STAT}" >> ${LOG_PROC}
    postload
    exit 1
fi


echo "PIRSFLoad application completed successfully" >> ${LOG_PROC}

postload

exit 0


###########################################################################
#
# Warranty Disclaimer and Copyright Notice
#
#  THE JACKSON LABORATORY MAKES NO REPRESENTATION ABOUT THE SUITABILITY OR
#  ACCURACY OF THIS SOFTWARE OR DATA FOR ANY PURPOSE, AND MAKES NO WARRANTIES,
#  EITHER EXPRESS OR IMPLIED, INCLUDING MERCHANTABILITY AND FITNESS FOR A
#  PARTICULAR PURPOSE OR THAT THE USE OF THIS SOFTWARE OR DATA WILL NOT
#  INFRINGE ANY THIRD PARTY PATENTS, COPYRIGHTS, TRADEMARKS, OR OTHER RIGHTS.
#  THE SOFTWARE AND DATA ARE PROVIDED "AS IS".
#
#  This software and data are provided to enhance knowledge and encourage
#  progress in the scientific community and are to be used only for research
#  and educational purposes.  Any reproduction or use for commercial purpose
#  is prohibited without the prior express written permission of The Jackson
#  Laboratory.
#
# Copyright \251 1996, 1999, 2002, 2004 by The Jackson Laboratory
#
# All Rights Reserved
#
###########################################################################
