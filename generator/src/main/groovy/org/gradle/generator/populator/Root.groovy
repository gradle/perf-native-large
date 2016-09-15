package org.gradle.generator.populator

import org.gradle.generator.model.Project

class Root {
    final File rootDir

    Root(File rootDir) {
        this.rootDir = rootDir
    }

    def populateWith(List<Project> projects, Map<String, Project> depsProviderMap) {
        rootDir.mkdirs()

        SettingsFile settings = new SettingsFile(rootDir, projects.size())

        projects.each { project ->
            settings.addProject(project)

            new Subproject(rootDir, project, depsProviderMap).populate()
        }
    }
}
