includeTargets << grailsScript("Init")

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

/*
    Runtime classpath for compiling drools rules, cannot contain the
    drools jars due to a classloader bug in the rulebase task.
 */
droolsCompileClasspath = {
    commonClasspath.delegate = delegate
    commonClasspath.call()

    def dependencies = grailsSettings.runtimeDependencies
    if (dependencies) {
        for (File f in dependencies) {
            if (f && !f.name.matches("drools-.*")) {
                pathelement(location: f.absolutePath)
            }
        }
    }

    pathelement(location: "${pluginClassesDir.absolutePath}")
    pathelement(location: "${classesDir.absolutePath}")
}

getRulesSourceFiles = {
    def drlFiles = resolveResources("file:${basedir}/descriptors/rules/*.drl").toList()
    def xlsFiles = resolveResources("file:${basedir}/descriptors/rules/*.xls").toList();

    return drlFiles + xlsFiles
}

getPkgFileName = { f ->
    return f.file.name.replaceAll("(\\.drl|\\.xls)", ".pkg")
}

target(compileRules: "Compiles DROOLS binary rules packages.") {
    ant.taskdef(name: "rulebase", classname: "org.drools.contrib.DroolsCompilerAntTask")
    ant.path(id: "drools.compile.classpath", droolsCompileClasspath)

    delete(dir: "./resources/rules")
    mkdir(dir: "./resources/rules")

    getRulesSourceFiles().each { f ->
        def pkgName = getPkgFileName(f)
        println "Compiling rules file ${f.file.name} to ${pkgName} ..."

        rulebase(srcdir: "./descriptors/rules/",
                 tofile: "./resources/rules/${pkgName}",
                 classpathref: "drools.compile.classpath",
                 binformat: "package") {

            include(name: f.file.name)
        }
    }
}

setDefaultTarget(compileRules)
