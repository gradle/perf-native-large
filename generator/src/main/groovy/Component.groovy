import groovy.transform.Canonical

@Canonical
class Component {
    GradleComponentType type
    int sources
    int headers
    int includePaths
    int dependencies
    // Only relevant for GradleComponentType.NATIVE_LIBRARY_SPEC
    boolean hasSharedLibrary
    boolean isGenerationComponent
}
