package org.gradle.generator.model

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
    // Only relevant for org.gradle.generator.model.GradleComponentType.NATIVE_LIBRARY_SPEC
    boolean hasSharedLibrary
    boolean hasStaticLibrary
    boolean hasApiLibrary
    List<Library> libraries
}
