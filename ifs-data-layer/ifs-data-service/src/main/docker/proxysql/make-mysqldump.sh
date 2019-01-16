#!/usr/bin/env bash

# take the data dump
echo "Taking dump"
mysqldump -u$DB_USER -p$DB_PASS -h127.0.0.1 -P6033 $DB_NAME --single-transaction --debug-info --ignore-table=$DB_NAME._ApplicationSetUp --ignore-table=$DB_NAME._ApplicationSetUp_CapUse --ignore-table=$DB_NAME._ApplicationSetUp_Value --ignore-table=$DB_NAME._tempIFS --ignore-table=$DB_NAME._tempIFS_AssScore --ignore-table=$DB_NAME._tempIFS_CapUse > /dump/anonymised-dump.sql

if [ "$?" -eq 1 ]; then
        echo "Taking mysql dump failed"
        exit 1
fi

# encrypt the dump file before transit and finally remove the unprotected file
gpg --yes --batch --passphrase=$DB_ANON_PASS -c /dump/anonymised-dump.sql
rm /dump/anonymised-dump.sql