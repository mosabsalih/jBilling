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

includeTargets << grailsScript("Init")

final tempDir = "${basedir}/tmp"
final resourcesDir = "${basedir}/resources"
final descriptorsDir = "${basedir}/descriptors"

target(compileDesigns: "Compiles jasper paper invoice designs.") {
    ant.taskdef(name: "jrc", classname: "net.sf.jasperreports.ant.JRAntCompileTask")

    delete(dir: "${resourcesDir}/designs")
    mkdir(dir: "${resourcesDir}/designs")

    mkdir(dir: tempDir)
    jrc(destdir: "${resourcesDir}/designs", tempdir: tempDir, keepjava: "true", xmlvalidation: "true") {
        src {
            fileset(dir: "${descriptorsDir}/designs", includes: "**/*.jrxml")
        }
    }
    delete(dir: tempDir)
}

setDefaultTarget(compileDesigns)
