/*
 jBilling - The Enterprise Open Source Billing System
 Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde

 This file is part of jbilling.

 jbilling is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 jbilling is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */

includeTargets << grailsScript("War")

includeTargets << new File("${basedir}/scripts/CopyResources.groovy")
includeTargets << new File("${basedir}/scripts/CompileDesigns.groovy")
includeTargets << new File("${basedir}/scripts/CompileReports.groovy")
includeTargets << new File("${basedir}/scripts/CompileRules.groovy")
includeTargets << new File("${basedir}/scripts/Jar.groovy")

resourcesDir = "${basedir}/resources"
configDir = "${basedir}/grails-app/conf"
sqlDir = "${basedir}/sql"
javaDir = "${basedir}/src/java"
targetDir = "${basedir}/target"

releaseName = "${grailsAppName}-${grailsAppVersion}"
packageName = "${targetDir}/${releaseName}.zip"

target(prepareRelease: "Builds the war and all necessary resources.") {
    copyResources()
    compileDesigns()
    compileReports()
    compileRules()
    jar()
    war()
}

target(packageRelease: "Builds the war and packages all the necessary config files and resources in a release zip file.") {
    depends(prepareRelease)

    // zip up resources into a release package
    delete(dir: targetDir, includes: "${grailsAppName}-*.zip")

    zip(filesonly: false, update: false, destfile: packageName) {
        zipfileset(dir: resourcesDir, prefix: "resources")
        zipfileset(dir: targetDir, includes: "${grailsAppName}.jar", prefix: "resources/api")
        zipfileset(dir: javaDir, includes: "jbilling.properties.sample", fullpath: "jbilling.properties")
        zipfileset(dir: configDir, includes: "Config.groovy", fullpath: "${grailsAppName}-Config.groovy")
        zipfileset(dir: configDir, includes: "DataSource.groovy", fullpath: "${grailsAppName}-DataSource.groovy")
        zipfileset(dir: targetDir, includes: "${grailsAppName}.war")
        zipfileset(dir: sqlDir, includes: "jbilling_test.sql")
    }

    println "Packaged release to ${packageName}"
}

setDefaultTarget(packageRelease)
