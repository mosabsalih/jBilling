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

includeTargets << grailsScript("Compile")

final targetDir = "${basedir}/target"

target(jar: "Packages all core jbilling classes in a .jar file.") {
    depends(compile)

    delete(dir: targetDir, includes: "${grailsAppName}.jar")

    exec(executable: "git", outputproperty: "version") {
        arg(line: "describe")
    }

    tstamp()
    ant.jar(destfile: "${targetDir}/${grailsAppName}.jar", basedir: "${targetDir}/classes") {
        manifest {
            attribute(name: "Built-By", value: System.properties.'user.name')
            attribute(name: "Built-On", value: "${DSTAMP}-${TSTAMP}")
            attribute(name: "Specification-Title", value: grailsAppName)
            attribute(name: "Specification-Version", value: grailsAppVersion)
            attribute(name: "Specification-Vendor", value: "jBilling.com")

            attribute(name: "Package-Title", value: grailsAppName)
            attribute(name: "Package-Version", value: version)
            attribute(name: "Package-Vendor", value: "jBilling.com")
        }
    }
}

setDefaultTarget(jar)
