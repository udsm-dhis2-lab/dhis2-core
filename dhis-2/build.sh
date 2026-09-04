#!/bin/sh

# Requires maven to be on the classpath
# Skips test phase

# mvn clean install --batch-mode --no-transfer-progress -DskipTests=true

mvn clean install -DskipTests=true
# mvn clean install -DskipTests=true -f dhis-web/pom.xml -U


