import groovy.transform.Canonical
import groovy.transform.CompileStatic

@CompileStatic
@Canonical
class Component {
    String name
    GradleComponentType type
    int sources
    int headers
    int includePaths
    List<String> dependencies
    // Only relevant for GradleComponentType.NATIVE_LIBRARY_SPEC
    boolean hasSharedLibrary
}
