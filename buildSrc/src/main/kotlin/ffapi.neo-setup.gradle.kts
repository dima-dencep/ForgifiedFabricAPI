import net.fabricmc.loom.api.LoomGradleExtensionAPI

val versionMc: String by rootProject
val versionForge: String by rootProject
val versionForgifiedFabricLoader: String by rootProject
val versionFabricLoader: String by rootProject

val loom = extensions.getByType<LoomGradleExtensionAPI>()
val sourceSets = extensions.getByType<SourceSetContainer>()

val jar = tasks.named<Jar>("jar")

val mainSourceSet = sourceSets.getByName("main")

mainSourceSet.apply {
    java {
        srcDir("src/client/java")
    }
    resources {
        srcDir("src/client/resources")
    }
}

val testmod: SourceSet by sourceSets.creating {
    compileClasspath += mainSourceSet.compileClasspath
    runtimeClasspath += mainSourceSet.runtimeClasspath

    java {
        srcDir("src/testmodClient/java")
    }
    resources {
        srcDir("src/testmodClient/resources")
    }
}

dependencies {
    // TODO Update gradle module metadata in FFLoader to avoid this
    "compileOnly"("org.sinytra:fabric-loader:$versionForgifiedFabricLoader")
    "runtimeOnly"("org.sinytra:fabric-loader:$versionForgifiedFabricLoader:full") {
        isTransitive = false
    }

    "testmodImplementation"(mainSourceSet.output)
    "testmodCompileOnly"("org.sinytra:fabric-loader:$versionForgifiedFabricLoader")
    "testmodRuntimeOnly"("org.sinytra:fabric-loader:$versionForgifiedFabricLoader:full") {
        isTransitive = false
    }

    "testImplementation"(testmod.output)
    "testImplementation"("org.mockito:mockito-core:5.4.0")
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.8.1")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    enabled = false
}

loom.apply {
    runtimeOnlyLog4j = true

    runs {
        configureEach {
            isIdeConfigGenerated = project.rootProject == project
            property("mixin.debug", "true")
            // FIXME Set this from fabric-api-base as ResourcePackProfileMixin fails otherwise
            property("mixin.initialiserInjectionMode", "safe")
        }

        create("gametest") {
            server()
            name = "Testmod Game Test Server"
            source(testmod)

            // Enable the gametest runner
            property("neoforge.gameTestServer", "true")
        }

        create("testmodClient") {
            client()
            name = "Testmod Client"
            source(testmod)
        }

        create("testmodServer") {
            server()
            name = "Testmod Server"
            source(testmod)
        }
    }
}
