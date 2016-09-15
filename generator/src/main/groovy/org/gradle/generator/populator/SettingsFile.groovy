package org.gradle.generator.populator

import groovy.transform.CompileStatic
import org.gradle.generator.model.Project

@CompileStatic
class SettingsFile {
    static final int INCLUDES_PER_LINE = 8
    static final String SETTINGS_FILE_NAME = "settings.gradle"

    final File settings
    final int includesPerLine
    final int maxProjects
    int includesCount

    SettingsFile(File rootDir, int maxProjects) {
        this(rootDir, maxProjects, INCLUDES_PER_LINE)
    }

    SettingsFile(File rootDir, int maxProjects, int includesPerLine) {
        this.settings = new File(rootDir, SETTINGS_FILE_NAME)
        this.includesPerLine = includesPerLine
        this.maxProjects = maxProjects
        includesCount = 0
        initialize()
    }

    def initialize() {
        settings.delete()
        settings << "rootProject.name = 'perf-native-large'\n"
    }

    def addProject(Project project) {
        if (includesCount % includesPerLine == 0) {
            settings << 'include '
        }
        settings << "'$project.name'"
        includesCount++
        if (includesCount % includesPerLine != 0  && includesCount < maxProjects) {
            settings << ', '
        } else {
            settings << '\n'
        }
    }
}
