package org.gradle.generator.model

import groovy.transform.CompileStatic

@CompileStatic
class Component {
    String name
    GradleComponentType type
    int sources
    int headers
    int includePaths
    List<String> dependencies
    // Relevant for NATIVE_LIBRARY_SPEC and PREBUILT_LIBRARY
    boolean hasApiLibrary
    boolean hasSharedLibrary
    boolean hasStaticLibrary
    // Only relevant for org.gradle.generator.model.GradleComponentType.NATIVE_LIBRARY_SPEC
    private Library apiLibrary
    private Library sharedLibrary
    private Library staticLibrary

    Component(String name) {
        this.name = name
        dependencies = []
        apiLibrary = null
        sharedLibrary = null
        staticLibrary = null
        hasApiLibrary = false
        hasSharedLibrary = false
        hasStaticLibrary = false
    }

    def boolean add(Library library) {
        if (canAdd(library)) {
            switch (library.linkage) {
                case Linkage.API:
                    apiLibrary = library
                    hasApiLibrary = true
                    break
                case Linkage.STATIC:
                    staticLibrary = library
                    hasStaticLibrary = true
                    break
                case Linkage.SHARED:
                    sharedLibrary = library
                    hasSharedLibrary = true
                    break
                default:
                    break
            }
            return true
        } else {
            return false
        }
    }

    private boolean canAdd(Library library) {
        (Linkage.API == library.linkage && !hasApiLibrary) ||
                (Linkage.SHARED == library.linkage && !hasSharedLibrary) ||
                (Linkage.STATIC == library.linkage && !hasStaticLibrary)
    }

    def List<Library> getLibraries() {
        List<Library> libraries = []
        if (hasApiLibrary) {
            libraries << apiLibrary
        }
        if (hasSharedLibrary) {
            libraries << sharedLibrary
        }
        if (hasStaticLibrary) {
            libraries << staticLibrary
        }
        return libraries
    }
}
