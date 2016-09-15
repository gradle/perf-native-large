package org.gradle.generator

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.gradle.generator.model.Project
import org.gradle.generator.parser.ReportParser
import org.gradle.generator.populator.SettingsFile

@CompileStatic
@TupleConstructor
class NativeLargeGenerator {
    File reportFile
    File outputDir

    void generate() {
        List<Project> projects = ReportParser.parse(reportFile)

        outputDir.mkdirs()

        SettingsFile settings = new SettingsFile(outputDir, projects.size())

        projects.each { project ->
            settings.addProject(project)

            File projectDir = new File(outputDir, project.name)
            projectDir.mkdir()
        }
    }

    static void main(String[] args) {
        def generator = new NativeLargeGenerator(new File('components.txt'), new File('/tmp/native-large'))
        generator.generate()
    }
}