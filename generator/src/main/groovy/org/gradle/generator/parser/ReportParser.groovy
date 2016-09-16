package org.gradle.generator.parser

import groovy.transform.CompileStatic
import org.gradle.generator.model.Component
import org.gradle.generator.model.GradleComponentType
import org.gradle.generator.model.Project
import org.gradle.generator.model.ReportModuleType

import java.util.regex.Pattern

@CompileStatic
class ReportParser {
    static Pattern COMPONENT_PATTERN = ~/# (\S+) \((.*)\) (.*)\.  Dependencies: (.*)$/
    static Pattern WHITESPACE_PATTERN = ~/\s+/

    static List<Project> parse(File reportFile, Map<String, Project> depsProviderMap) {
        def tracker = new ComponentTracker()
        List<Project> projects = []
        Project currentProject
        reportFile.eachLine { String line ->
            line = line.trim()
            if (line) {
                if (line.startsWith('# project')) {
                    currentProject = new Project(number: line.substring('# project'.length()).trim() as int)
                    tracker.reset()
                    projects << currentProject
                } else {
                    def m = (line =~ COMPONENT_PATTERN)
                    if (m.matches()) {
                        def name = sanitizeName(m.group(1))
                        ReportModuleType moduleType = ReportModuleType.from(m.group(2))
                        assert moduleType != null : "$currentProject.name has unexpected Module Type: ${m.group(2)}"
                        // Useful to build this map now to efficiently get projects which provide dependencies later.
                        depsProviderMap.put(name, currentProject)
                        Component component = componentFrom(name, moduleType)
                        if (GradleComponentType.PREBUILT_LIBRARY == component.type) {
                            currentProject.prebuiltLibraries << component
                        } else if (GradleComponentType.GOOGLE_TEST_TEST_SUITE_SPEC == component.type) {
                            currentProject.testSuites << component
                        } else {
                            if (tracker.shouldAdd(component, moduleType)) {
                                currentProject.components << component
                            }
                        }
                        for (List attributePair : (WHITESPACE_PATTERN.split(m.group(3)) as List).collate(2)) {
                            int value = attributePair.get(0) as int
                            String field = attributePair.get(1)
                            component.setProperty(field, value)
                        }
                        def deps = m.group(4)
                        if (deps.startsWith('[')) {
                            deps = deps.substring(1, deps.length() - 1)
                            for (String dep : deps.split(', ')) {
                                component.dependencies << sanitizeName(dep)
                            }
                        }
                    }
                }
            }
        }
        projects
    }

    static Component componentFrom(String name, ReportModuleType type) {
        Component toReturn = new Component(name)
        switch (type) {
            case ReportModuleType.SHARED_LIBRARY:
            case ReportModuleType.CUDA_SHARED_LIBRARY:
            case ReportModuleType.STATIC_LIBRARY:
            case ReportModuleType.CUDA_STATIC_LIBRARY:
            case ReportModuleType.API_LIBRARY:
                toReturn.type = GradleComponentType.NATIVE_LIBRARY_SPEC
                break
            case ReportModuleType.EXECUTABLE:
            case ReportModuleType.CUDA_EXECUTABLE:
                if (name.startsWith('test')) {
                    toReturn.type = GradleComponentType.GOOGLE_TEST_TEST_SUITE_SPEC
                } else {
                    toReturn.type = GradleComponentType.NATIVE_EXECUTABLE_SPEC
                }
                break
            case ReportModuleType.PREBUILT_SHARED:
                toReturn.type = GradleComponentType.PREBUILT_LIBRARY
                toReturn.hasSharedLibrary = true
                toReturn.hasStaticLibrary = true
                toReturn.hasApiLibrary = true
                break
            case ReportModuleType.PREBULIT_STATIC:
                toReturn.type = GradleComponentType.PREBUILT_LIBRARY
                toReturn.hasStaticLibrary = true
                toReturn.hasApiLibrary = true
                break
            case ReportModuleType.PREBUILT_API:
                toReturn.type = GradleComponentType.PREBUILT_LIBRARY
                toReturn.hasApiLibrary = true
                break
            default:
                break
        }
        toReturn
    }

    static String sanitizeName(String name) {
        StringBuilder builder = new StringBuilder()
        def justSawAHyphen = false
        for (char c : name.toCharArray()) {
            if (c != '-') {
                builder.append(justSawAHyphen ? c.toUpperCase() : c)
                justSawAHyphen = false
            } else {
                justSawAHyphen = true
            }
        }
        builder.toString()
    }
}
