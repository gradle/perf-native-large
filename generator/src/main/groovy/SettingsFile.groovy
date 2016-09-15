class SettingsFile {
    static final int INCLUDES_PER_LINE = 8

    final File settings
    final int includesPerLine
    final int maxProjects
    int includesCount

    SettingsFile(File settings, int maxProjects) {
        this(settings, maxProjects, INCLUDES_PER_LINE)
    }

    SettingsFile(File settings, int maxProjects, int includesPerLine) {
        this.settings = settings
        this.includesPerLine = includesPerLine
        this.maxProjects = maxProjects
        includesCount = 0
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
