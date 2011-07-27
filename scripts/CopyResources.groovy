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

final resourcesDir = "${basedir}/resources"
final descriptorsDir = "${basedir}/descriptors"

target(cleanResources: "Removes the existing jbilling resources directory.") {
    delete(dir: "${resourcesDir}")
}

target(createStructure: "Creates the jbilling resources directory structure.") {
    ant.sequential {
        mkdir(dir: "${resourcesDir}")
        mkdir(dir: "${resourcesDir}/api")
        mkdir(dir: "${resourcesDir}/designs")
        mkdir(dir: "${resourcesDir}/invoices")
        mkdir(dir: "${resourcesDir}/logos")
        mkdir(dir: "${resourcesDir}/mediation")
        mkdir(dir: "${resourcesDir}/mediation/errors")
        mkdir(dir: "${resourcesDir}/reports")
        mkdir(dir: "${resourcesDir}/rules")
    }
}

target(copyResources: "Creates the jbilling 'resources/' directories and copies necessary files.") {
    depends(cleanResources, createStructure)

    // copy default company logos
    copy(todir: "${resourcesDir}/logos") {
        fileset(dir: "${descriptorsDir}/logos")
    }

    // copy default mediation files
    copy(todir: "${resourcesDir}/mediation") {
        fileset(dir: "${descriptorsDir}/mediation", includes: "mediation.dtd")
        fileset(dir: "${descriptorsDir}/mediation", includes: "asterisk.xml")
    }

    // preserve empty directories when zipping
    touch(file: "${resourcesDir}/invoices/emptyfile.txt")
    touch(file: "${resourcesDir}/mediation/errors/emptyfile.txt")
    touch(file: "${resourcesDir}/rules/emptyfile.txt")
}

setDefaultTarget(copyResources)
