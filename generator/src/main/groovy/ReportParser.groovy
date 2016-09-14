import groovy.transform.CompileStatic

import java.util.regex.Pattern

@CompileStatic
class ReportParser {
    static Pattern COMPONENT_PATTERN = ~/# (\S+) \((.*)\) (.*)\.  Dependencies: \[(.*)\]$/
    static Pattern WHITESPACE_PATTERN = ~/\s+/

    static List<Project> parse(File reportFile) {
        List<Project> projects = []
        Project currentProject
        reportFile.eachLine { String line ->
            line = line.trim()
            if (line) {
                if (line.startsWith('# project')) {
                    currentProject = new Project(number: line.substring('# project'.length()).trim() as int)
                    projects << currentProject
                } else {
                    def m = (line =~ COMPONENT_PATTERN)
                    if (m.matches()) {
                        def name = m.group(1)
                        ReportModuleType moduleType = ReportModuleType.from(m.group(2))
                        assert moduleType != null : "$currentProject.name has unexpected Module Type: $moduleType"
                        Component component = componentFrom(name, moduleType)
                        currentProject.components << component
                        for (List attributePair : (WHITESPACE_PATTERN.split(m.group(3)) as List).collate(2)) {
                            int value = attributePair.get(0) as int
                            String field = attributePair.get(1)
                            component.setProperty(field, value)
                        }
                        for (String dep : m.group(4).split(', ')) {
                            component.dependencies << dep
                        }
                    }
                }
            }
        }
        projects
    }

    static Component componentFrom(String name, ReportModuleType type) {
        Component toReturn = new Component(name: name, dependencies: [])
        switch (type) {
            case ReportModuleType.STATIC_LIBRARY:
            case ReportModuleType.CUDA_STATIC_LIBRARY:
                toReturn.type = GradleComponentType.NATIVE_LIBRARY_SPEC
                toReturn.hasSharedLibrary = false
                break
            case ReportModuleType.SHARED_LIBRARY:
            case ReportModuleType.CUDA_SHARED_LIBRARY:
                toReturn.type = GradleComponentType.NATIVE_LIBRARY_SPEC
                toReturn.hasSharedLibrary = true
                break
            case ReportModuleType.EXECUTABLE:
            case ReportModuleType.CUDA_EXECUTABLE:
                if (name.startsWith('test-')) {
                    toReturn.type = GradleComponentType.GOOGLE_TEST_TEST_SUITE_SPEC
                } else {
                    toReturn.type = GradleComponentType.NATIVE_EXECUTABLE_SPEC
                }
                break
            case ReportModuleType.PREBUILT_LIBRARY:
                toReturn.type = GradleComponentType.PREBUILT_LIBRARY
                break
            default:
                break
        }
        toReturn
    }
}
