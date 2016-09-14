import groovy.transform.Canonical
import groovy.transform.TupleConstructor

import java.util.regex.Pattern

@TupleConstructor
class NativeLargeGenerator {
    File reportFile
    File outputDir

    void generate() {
        outputDir.mkdirs()
        List<Project> projects = ReportParser.parse(reportFile)
        println projects
    }

    static void main(String[] args) {
        def generator = new NativeLargeGenerator(new File('report.txt'), new File('/tmp/native-large'))
        generator.generate()
    }
}


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
                    if (currentProject != null) {
                        projects << currentProject
                    }
                    currentProject = new Project()
                    currentProject.name = line.substring('project '.length()).trim()
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
        if (currentProject != null) {
            projects << currentProject
        }
    }
}

@Canonical
class Project {
    String name
    List<Module> modules = []
}

@Canonical
class Module {
    String type
    int sources
    int headers
    int includePaths
    int dependencies
}