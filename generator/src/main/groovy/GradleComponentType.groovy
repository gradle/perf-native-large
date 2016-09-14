import groovy.transform.CompileStatic

@CompileStatic
enum GradleComponentType {
    GOOGLE_TEST_TEST_SUITE_SPEC("GoogleTestTestSuiteSpec", true),
    NATIVE_EXECUTABLE_SPEC("NativeExecutableSpec", true),
    NATIVE_LIBRARY_SPEC("NativeLibrarySpec", true),
    NATIVE_LIBRARY_SPEC_UNSHARED("NativeLibrarySpec", false)

    final String name
    final boolean sharedLibraryEnabled
    static final Map<String, GradleComponentType> reverseMap

    static {
        reverseMap = [:] as TreeMap
        values().each { GradleComponentType type ->
            reverseMap.put(type.name, type)
        }
    }

    private GradleComponentType(String name, boolean sharedLibraryEnabled) {
        this.name = name
        this.sharedLibraryEnabled = sharedLibraryEnabled
    }


    static GradleComponentType from(String type) {
        reverseMap[type]
    }
}
