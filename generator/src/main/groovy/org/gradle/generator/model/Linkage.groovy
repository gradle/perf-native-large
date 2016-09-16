package org.gradle.generator.model

import groovy.transform.CompileStatic

@CompileStatic
enum Linkage {
    STATIC("static"),
    SHARED("shared"),
    API("api")

    final String linkage

    Linkage(String linkage) {
        this.linkage = linkage
    }
}
