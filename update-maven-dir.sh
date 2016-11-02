#!/bin/sh

VERSION=5.0.0.0-SNAPSHOT

mvn clean install -DskipTests

mvn install:install-file -DgroupId=org.kisses -DartifactId=kisses-core -Dversion=$VERSION -Dpackaging=jar -Dfile=kisses-core/target/kisses-core-$VERSION.jar -DlocalRepositoryPath=maven
mvn install:install-file -DgroupId=org.kisses -DartifactId=kisses-annotations -Dversion=$VERSION -Dpackaging=jar -Dfile=kisses-annotations/target/kisses-annotations-$VERSION.jar -DlocalRepositoryPath=maven/
mvn install:install-file -DgroupId=org.kisses -DartifactId=kisses-spring4 -Dversion=$VERSION -Dpackaging=jar -Dfile=kisses-spring4/target/kisses-spring4-$VERSION.jar -DlocalRepositoryPath=maven/