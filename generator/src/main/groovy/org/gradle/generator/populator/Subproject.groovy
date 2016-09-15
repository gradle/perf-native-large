package org.gradle.generator.populator

import groovy.transform.CompileStatic
import org.gradle.generator.model.Component
import org.gradle.generator.model.Project

@CompileStatic
class Subproject {
    final Project project
    final File rootDir

    Subproject(File rootDir, Project project) {
        this.rootDir = rootDir
        this.project = project
    }

    def populate() {
        def projectDir = new File(rootDir, project.name)
        projectDir.deleteDir()
        projectDir.mkdir()

        def buildFile = new File(projectDir, "build.gradle")
        buildFile.delete()
        def writer = buildFile.newWriter()
        writer << '''plugins {
    id 'cpp'
}

model {
    buildTypes {
        debug
        release
        prod
    }
'''
        if (project.components) {
            writer << '    components {\n'
            project.components.each { Component component ->
                writer << "        ${component.name}(${component.type.name}) {\n"
                writer << '        }\n'
            }
            writer << '    }\n'
        }
        writer << '}\n'
        writer.close()
    }
}
