import groovy.transform.CompileStatic

import java.util.regex.Pattern

@CompileStatic
class ReportParser {
    static Pattern MODULE_PATTERN = ~/# (\S+) with (.*)$/
    static Pattern WHITESPACE_PATTERN = ~/\s+/

    static List<Project> parse(File reportFile) {
        List<Project> projects = []
        Project currentProject
        reportFile.eachLine { String line ->
            line = line.trim()
            if (line) {
                if (line.startsWith('project ')) {
                    currentProject = new Project(number: line.substring('project '.length()).trim() as int)
                    projects << currentProject
                } else {
                    def m = (line =~ MODULE_PATTERN)
                    if (m.matches()) {
                        def moduleType = m.group(1)
                        Module module = new Module(type: moduleType)
                        currentProject.modules << module
                        for (List attributePair : (WHITESPACE_PATTERN.split(m.group(2)) as List).collate(2)) {
                            int value = attributePair.get(0) as int
                            String field = attributePair.get(1)
                            module.setProperty(field, value)
                        }
                    }
                }
            }
        }
        projects
    }
}
