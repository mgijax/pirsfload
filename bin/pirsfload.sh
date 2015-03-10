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
CONFIG=`pwd`/pirsfload.config

#
#  Make sure the configuration files are readable.
#
if [ ! -r ${CONFIG} ]
then
    echo "Cannot read configuration file: ${CONFIG}" | tee -a ${LOG}
    exit 1
fi
. ${CONFIG}

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
# Set and verify the master configuration file name
#
CONFIG_MASTER=${MGICONFIG}/master.config.sh
if [ ! -r ${CONFIG_MASTER} ]
then
    echo "Cannot read configuration file: ${CONFIG_MASTER}" | tee -a ${LOG}
    exit 1
fi

#
#  Perform pre-load tasks.
#
preload

#
#  Run the load application.
#
echo "" >> ${LOG_PROC}
echo "`date`" >> ${LOG_PROC}
echo "Run the PIRSFLoad application" >> ${LOG_PROC}
/usr/local/bin/gunzip -c ${INPUT_FILENAME}|${JAVA} ${JAVARUNTIMEOPTS} -classpath ${CLASSPATH} \
        -DCONFIG=${CONFIG_MASTER},${CONFIG} \
        -DJOBKEY=${JOBKEY} ${DLA_START}
STAT=$?
if [ ${STAT} -ne 0 ]
then
    echo "PIRSFLoad application failed.  Return status: ${STAT}" >> ${LOG_PROC}
    postload
    exit 1
fi

echo "" >> ${LOG_PROC}
echo "`date`" >> ${LOG_PROC}
echo "Run the Vocabulary load" >> ${LOG_PROC}
${VOCLOAD}/runSimpleFullLoadNoArchive.sh ${VOCLOAD_CONFIG} >> ${LOG_PROC}
STAT=$?
if [ ${STAT} -ne 0 ]
then
    echo "PIRSFLoad application failed during vocabulary load.  Return status: ${STAT}" >> ${LOG_PROC}
    postload
    exit 1
fi

echo "" >> ${LOG_PROC}
echo "`date`" >> ${LOG_PROC}
echo "Run the Annotation load" >> ${LOG_PROC}
cd ${OUTPUTDIR} && ${ANNOTLOAD}/annotload.csh ${ANNOTLOAD_CONFIG} >> ${LOG_PROC}
STAT=$?
if [ ${STAT} -ne 0 ]
then
    echo "PIRSFLoad application failed during annotation load.  Return status: ${STAT}" >> ${LOG_PROC}
    postload
    exit 1
fi

#
# run qc reports
#
${APP_QCRPT} ${RPTDIR} ${JOBKEY}
STAT=$?
if [ ${STAT} -ne 0 ]
then
    echo "Running QC reports failed.	Return status: ${STAT}" >> ${LOG_PROC}
    postload
    exit 1
fi

echo "PIRSFLoad application completed successfully" >> ${LOG_PROC}

postload

exit 0

