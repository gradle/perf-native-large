package org.gradle.generator.model

import groovy.transform.CompileStatic

@CompileStatic
enum Linkage {
    STATIC("static"),
    SHARED("shared"),
    API("api")

    final String name

    Linkage(String name) {
        this.name = name
    }
}
