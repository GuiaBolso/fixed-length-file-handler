import com.novoda.gradle.release.PublishExtension
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
    
    dependencies {
        classpath("com.novoda:bintray-release:0.9.1")
    }
}

plugins {
    kotlin("jvm") version "1.3.50"
    
    `maven-publish`
    id("org.jetbrains.dokka") version "0.9.17"
    id("io.gitlab.arturbosch.detekt").version("1.1.1")
}

apply(plugin = "com.novoda.bintray-release")

group = "br.com.guiabolso"
version = "0.2.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    
    // KotlinTest
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.getByName("main").allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    val javadoc = tasks["dokka"] as DokkaTask
    javadoc.outputFormat = "javadoc"
    javadoc.outputDirectory = "$buildDir/javadoc"
    dependsOn(javadoc)
    classifier = "javadoc"
    from(javadoc.outputDirectory)
}

detekt {
    toolVersion = "1.1.1"
    input = files("src/main/kotlin", "src/test/kotlin")
}

publishing {
    publications {
        
        register("maven", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
            
            pom {
                name.set("Fixed-Length-File-Handler")
                description.set("Fixed-Length-File-Handler")
                url.set("https://github.com/GuiaBolso/fixed-length-file-handler")
                
                
                scm {
                    connection.set("scm:git:https://github.com/GuiaBolso/fixed-length-file-handler/")
                    developerConnection.set("scm:git:https://github.com/GuiaBolso/")
                    url.set("https://github.com/GuiaBolso/fixed-length-file-handler")
                }
                
                licenses {
                    license {
                        name.set("The Apache 2.0 License")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
            }
        }
    }
}

configure<PublishExtension> {
    artifactId = "fixed-length-file-handler"
    autoPublish = true
    desc = "Fixed Length File Handler"
    groupId = "br.com.guiabolso"
    userOrg = "gb-opensource"
    setLicences("APACHE-2.0")
    publishVersion = version.toString()
    uploadName = "Fixed-Length-File-Handler"
    website = "https://github.com/GuiaBolso/fixed-length-file-handler"
    setPublications("maven")
}