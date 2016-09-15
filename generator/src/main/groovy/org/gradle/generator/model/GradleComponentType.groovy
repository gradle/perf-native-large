package org.gradle.generator.model

import groovy.transform.CompileStatic

@CompileStatic
enum GradleComponentType {
    GOOGLE_TEST_TEST_SUITE_SPEC("GoogleTestTestSuiteSpec"),
    NATIVE_EXECUTABLE_SPEC("NativeExecutableSpec"),
    NATIVE_LIBRARY_SPEC("NativeLibrarySpec"),
    PREBUILT_LIBRARY("PrebuiltLibrary")

    final String name

    private GradleComponentType(String name) {
        this.name = name
    }
}
