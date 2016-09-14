import groovy.transform.CompileStatic

import static GradleComponentType.*
import static ReportModuleType.*

/**
 * Decoder Rules:
 * 1. if there's nothing under the project, it didn't have components
 * 2. if there's a `CMakeExecutable` it's a NativeExecutableSpec.  If there are two, it's a NativeExecutableSpec and a GoogleTestTestSuiteSpec
 * 3. if there's a `CMakeSharedLibrary` and a `CMakeStaticLibrary` and a `CMakeApiLibrary`, the project has a single NativeLibrarySpec
 * 4. if it only has a `CMakeStaticLibrary` and a `CMakeApiLibrary`, the project has a single NativeLibrarySpec and has disabled the shared library
 * 5. either of 3 or 4 with a `CMakeExecutable`, then it has a test (GoogleTestTestSuiteSpec)
 * 6. if there are multiple sets of `CMakeSharedLibrary` and a `CMakeStaticLibrary` and a `CMakeApiLibrary`, then there are multiple NativeLibrarySpecs
 * 7. if there are a hell of a lot of `CMakeImportedLibrary`, that's the project with PrebuiltLibraries
 *
 * message generation projects show up as "regular" libraries with 0 sources and some headers
 * message generation projects generally have 3 generated libraries (protobuf, and two internal ones)
 */
@CompileStatic
class ProjectClassifier {

    static classify(List<Project> projects) {
        projects.each { project ->
            Map<ReportModuleType, List<Module>> projectModules = [:]
            project.modules.each { module ->
                projectModules.putIfAbsent(module.type, [])
                projectModules[module.type].add(module)
            }
            // If the only things in a project are CMakeExecutables, then the first one is a NativeExecutableSpec.
            if (!project.modules.isEmpty() && getSafeList(C_MAKE_EXECUTABLE, projectModules).size() == project.modules.size()) {
                project.components.add(extractNativeExecutableSpec(projectModules))
            }
            // If there is a CMakeSharedLibrary, then there must also be a CMakeStaticLibrary and CMakeAPILibrary
            // which will be combined into a single NativeLibrarySpec
            while (getSafeList(C_MAKE_SHARED_LIBRARY, projectModules).size() > 0) {
                project.components.add(extractNativeLibrarySpec(projectModules))
            }
            // If there are still CMakeStaticLibraries, then each represents a NativeLibrarySpec with the shared
            // library disabled.
            while (getSafeList(C_MAKE_STATIC_LIBRARY, projectModules).size() > 0) {
                project.components.add(extractUnsharedNativeLibrarySpec(projectModules))
            }
            while (getSafeList(C_MAKE_IMPORTED_LIBRARY, projectModules).size() > 0) {
                project.components.add(extractPrebuiltLibrary(projectModules))
            }
            // There may also be a GoogleTestTestSuiteSpec
            if (!getSafeList(C_MAKE_EXECUTABLE, projectModules).isEmpty()) {
                project.components.add(extractGoogleTestSuiteSpec(projectModules))
            }
            // In rare cases, there is still an extra C_MAKE_EXECUTABLE that represents a NativeExecutable
            if (!getSafeList(C_MAKE_EXECUTABLE, projectModules).isEmpty()) {
                project.components.add(extractNativeExecutableSpec(projectModules))
            }
            projectModules.each { ReportModuleType type, List<Module> modules ->
                assert modules.isEmpty() : project.name
            }
            project.modules.clear()
        }
    }

    static def List<Module> getSafeList(ReportModuleType type, Map<ReportModuleType, List<Module>> projectModules) {
        projectModules.getOrDefault(type, [])
    }

    static Component extractPrebuiltLibrary(Map<ReportModuleType, List<Module>> projectModules) {
        Module importedLibrary = projectModules[C_MAKE_IMPORTED_LIBRARY].pop()
        new Component(
                type: PREBUILT_LIBRARY,
                sources: importedLibrary.sources,
                headers: importedLibrary.headers,
                includePaths: importedLibrary.includePaths,
                dependencies: importedLibrary.dependencies
        )
    }

    static Component extractGoogleTestSuiteSpec(Map<ReportModuleType, List<Module>> projectModules) {
        extractExecutableOfType(GOOGLE_TEST_TEST_SUITE_SPEC, projectModules)
    }

    static Component extractNativeExecutableSpec(Map<ReportModuleType, List<Module>> projectModules) {
        extractExecutableOfType(NATIVE_EXECUTABLE_SPEC, projectModules)
    }

    static Component extractExecutableOfType(GradleComponentType type, Map<ReportModuleType, List<Module>> projectModules) {
        Module executable = projectModules[C_MAKE_EXECUTABLE].pop()
        new Component(
                type: type,
                sources: executable.sources,
                headers: executable.headers,
                includePaths: executable.includePaths,
                dependencies: executable.dependencies
        )
    }

    static Component extractUnsharedNativeLibrarySpec(Map<ReportModuleType, List<Module>> projectModules) {
        extractNativeLibrarySpec(projectModules, false)
    }

    static Component extractNativeLibrarySpec(Map<ReportModuleType, List<Module>> projectModules) {
        extractNativeLibrarySpec(projectModules, true)
    }

    static Component extractNativeLibrarySpec(Map<ReportModuleType, List<Module>> projectModules, boolean hasShardLibrary) {
        Module staticLibrary = projectModules[C_MAKE_STATIC_LIBRARY].pop()
        projectModules[C_MAKE_API_LIBRARY].pop()
        if (hasShardLibrary) {
            projectModules[C_MAKE_SHARED_LIBRARY].pop()
        }
        def isGenerationComponent = staticLibrary.sources == 0 && staticLibrary.headers > 0
        new Component(
                type: GradleComponentType.NATIVE_LIBRARY_SPEC,
                sources: staticLibrary.sources,
                headers: staticLibrary.headers,
                includePaths: staticLibrary.includePaths,
                dependencies: staticLibrary.dependencies,
                hasSharedLibrary: hasShardLibrary,
                isGenerationComponent: isGenerationComponent
        )
    }
}