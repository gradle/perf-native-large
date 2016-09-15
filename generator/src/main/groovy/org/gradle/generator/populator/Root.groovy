package org.gradle.generator.populator

import org.gradle.generator.model.Project

class Root {
    final File rootDir

    Root(File rootDir) {
        this.rootDir = rootDir
    }

    def populateWith(List<Project> projects) {
        rootDir.mkdirs()

        SettingsFile settings = new SettingsFile(rootDir, projects.size())

        projects.each { project ->
            settings.addProject(project)

            File projectDir = new File(rootDir, project.name)
            projectDir.mkdir()
        }
    }
}
