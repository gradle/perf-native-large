package org.gradle.generator

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.gradle.generator.model.Project
import org.gradle.generator.parser.ReportParser
import org.gradle.generator.populator.Root

@CompileStatic
@TupleConstructor
class NativeLargeGenerator {
    File reportFile
    File outputDir

    void generate() {
        Map<String, Project> depsProviderMap = [:]
        List<Project> projects = ReportParser.parse(reportFile, depsProviderMap)

        new Root(outputDir).populateWith(projects, depsProviderMap)
    }

    static void main(String[] args) {
        def generator = new NativeLargeGenerator(new File('components.txt'), new File('../'))
        generator.generate()
    }
}
