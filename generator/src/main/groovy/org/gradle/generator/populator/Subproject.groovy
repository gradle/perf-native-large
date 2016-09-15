package org.gradle.generator.populator

import groovy.text.SimpleTemplateEngine
import groovy.transform.CompileStatic
import org.gradle.generator.model.Component
import org.gradle.generator.model.GradleComponentType
import org.gradle.generator.model.Project

@CompileStatic
class Subproject {
    static final File SRC_CPP = new File("src/main/templates/src.cpp")
    static final File SRC_H = new File("src/main/templates/src.h")
    static final File BIN_CPP = new File("src/main/templates/bin.cpp")
    final SimpleTemplateEngine engine
    final Project project
    final File rootDir

    Subproject(File rootDir, Project project) {
        this.rootDir = rootDir
        this.project = project
        this.engine = new SimpleTemplateEngine()
    }

    def populate() {
        def projectDir = new File(rootDir, project.name)
        projectDir.deleteDir()
        projectDir.mkdir()

        writeBuildFile(projectDir)
        writeSourceFiles(projectDir)
    }

    def writeSourceFiles(File projectDir) {
        project.components.each { Component component ->
            writeFilesForComponent(projectDir, component)
        }
        project.testSuites.each { Component component ->
            writeFilesForComponent(projectDir, component)
        }
    }

    def writeFilesForComponent(File projectDir, Component component) {
        def cppDir = new File(projectDir, "src/$component.name/cpp")
        cppDir.mkdirs()
        def needAMain = (GradleComponentType.NATIVE_EXECUTABLE_SPEC == component.type
                || GradleComponentType.GOOGLE_TEST_TEST_SUITE_SPEC == component.type)
        def numberOfSources = component.sources
        numberOfSources.times {
            def binding = ["component" : component.name, "it": it]
            // We'll make the first source file have a main.
            if (it == 0) {
                def src = new File(cppDir, "main.cpp")
                src << engine.createTemplate(BIN_CPP).make(binding)
            } else {
                def src = new File(cppDir, "lib${it + 1}.cpp")
                src << engine.createTemplate(SRC_CPP).make(binding)
            }
        }
        def headersDir = new File(projectDir, "src/$component.name/headers")
        headersDir.mkdirs()
        def numberOfHeaders = component.headers
        numberOfHeaders.times {
            def binding = ["component" : component.name, "it": it]
            def hFile = new File(headersDir, "lib${it+1}.h")
            hFile << engine.createTemplate(SRC_H).make(binding)
        }
    }

    def writeBuildFile(File projectDir) {
        def buildFile = new File(projectDir, "build.gradle")
        buildFile.delete()
        def writer = buildFile.newWriter()
        writer << 'plugins {\n'
        writer << "    id 'cpp'\n"
        if (project.testSuites) {
            writer << "    id 'google-test-test-suite'"
        }
        writer << '''}

model {
    buildTypes {
        debug
        release
        prod
    }
'''
        if (project.prebuiltLibraries) {
            writer << '    repositories { \n'
            writer << '        libs(PrebuiltBinaries) {\n'
            project.prebuiltLibraries.each { Component component ->
                writer << "            ${component.name}\n"
            }
            writer << '        }\n'
            writer << '    }\n'
        }
        if (project.components) {
            writer << '    components {\n'
            project.components.each { Component component ->
                writer << "        ${component.name}(${component.type.name}) {\n"
                if (!component.hasSharedLibrary) {
                    writer << '            binaries.withType(SharedLibraryBinarySpec) {\n'
                    writer << '                buildable = false'
                    writer << '            }'
                }
                writer << '        }\n'
            }
            writer << '    }\n'
        }
        if (project.testSuites) {
            writer << '    testSuites {\n'
            project.testSuites.each { Component component ->
                writer << "        ${component.name}(${component.type.name}) {\n"
                writer << "            testing \$.components.${project.components.first().name}"
                writer << '        }\n'
            }
            writer << '    }\n'
        }
        writer << '}\n'
        writer.close()
    }
}
