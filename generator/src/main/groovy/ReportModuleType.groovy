import groovy.transform.CompileStatic

@CompileStatic
enum ReportModuleType {
    C_MAKE_EXECUTABLE("CMakeExecutable"),
    C_MAKE_SHARED_LIBRARY("CMakeSharedLibrary"),
    C_MAKE_STATIC_LIBRARY("CMakeStaticLibrary"),
    C_MAKE_API_LIBRARY("CMakeApiLibrary"),
    C_MAKE_IMPORTED_LIBRARY("CMakeImportedLibrary")

    final String name
    static final Map<String, ReportModuleType> reverseMap

    static {
        reverseMap = [:] as TreeMap
        values().each { ReportModuleType type ->
            reverseMap.put(type.name, type)
        }
    }

    private ReportModuleType(String name) {
        this.name = name
    }

    static ReportModuleType from(String type) {
        reverseMap[type]
    }
}
