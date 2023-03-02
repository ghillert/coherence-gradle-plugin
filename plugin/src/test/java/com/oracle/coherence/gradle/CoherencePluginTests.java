/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.oracle.coherence.gradle.support.TestUtils.appendToFile;
import static com.oracle.coherence.gradle.support.TestUtils.getPofClass;
import static com.oracle.coherence.gradle.support.TestUtils.assertThatClassIsPofIntrumented;
import static com.oracle.coherence.gradle.support.TestUtils.copyFileTo;

import static org.assertj.core.api.Assertions.assertThat;

public class CoherencePluginTests
    {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoherencePluginTests.class);

    @TempDir
    private File gradleProjectRootDirectory;

    @BeforeEach
    void setup()
        {
        LOGGER.info("Gradle root directory for test: {}", gradleProjectRootDirectory.getAbsolutePath());
        }

    @Test
    void applyBasicCoherenceGradlePluginWithNoSources()
        {

        final File buildFile = new File(gradleProjectRootDirectory, "build.gradle");

        appendToFile(buildFile,
                """
                plugins {
                  id 'java'
                  id 'com.oracle.coherence.gradle'
                }
                """
                );

        BuildResult gradleResult = GradleRunner.create()
                .withProjectDir(gradleProjectRootDirectory)
                .withArguments("coherencePof")
                .withDebug(true)
                .withPluginClasspath()
                .build();

        LOGGER.info(
             "\n-------- [ Gradle output] -------->>>>\n"
            + gradleResult.getOutput()
            + "<<<<------------------------------------"
        );

        assertThat(gradleResult.getOutput()).contains("SUCCESS");
        assertThat(gradleResult.task(":coherencePof").getOutcome().name()).isEqualTo("SUCCESS");
        }

    @Test
    void applyBasicCoherenceGradlePluginWithClass()
        {

        final File buildFile = new File(gradleProjectRootDirectory, "build.gradle");

        appendToFile(buildFile,
            """
                    plugins {
                      id 'java'
                      id 'com.oracle.coherence.gradle'
                    }
                    repositories {
                        mavenCentral()
                    }
                    dependencies {
                        implementation 'com.oracle.coherence.ce:coherence:22.09'
                    }
                    """
        );

        copyFileTo("/Foo.txt", gradleProjectRootDirectory,
                "/src/main/java", "Foo.java");

        BuildResult gradleResult = GradleRunner.create()
                .withProjectDir(gradleProjectRootDirectory)
                .withArguments("coherencePof")
                .withDebug(true)
                .withPluginClasspath()
                .build();

        LOGGER.info(
                  "\n-------- [ Gradle output] -------->>>>\n"
                + gradleResult.getOutput()
                + "<<<<------------------------------------"
        );

        assertThat(gradleResult.getOutput()).contains("SUCCESS");
        assertThat(gradleResult.task(":coherencePof").getOutcome().name()).isEqualTo("SUCCESS");

        Class foo = getPofClass(this.gradleProjectRootDirectory, "Foo", "build/classes/java/main/");
        assertThatClassIsPofIntrumented(foo);
        }

    @Test
    void applyCoherenceGradlePluginWithTestClass()
        {

        final File buildFile = new File(gradleProjectRootDirectory, "build.gradle");

        appendToFile(buildFile,
            """
                    plugins {
                      id 'java'
                      id 'com.oracle.coherence.gradle'
                    }
                    repositories {
                        mavenCentral()
                    }
                    dependencies {
                        implementation 'com.oracle.coherence.ce:coherence:22.09'
                    }
                    coherencePof {
                        instrumentTestClasses = true
                    }
                    """
        );

        copyFileTo("/Foo.txt", gradleProjectRootDirectory,
                "/src/main/java", "Foo.java");

        copyFileTo("/Bar.txt", gradleProjectRootDirectory,
                "/src/test/java", "Bar.java");

        BuildResult gradleResult = GradleRunner.create()
                .withProjectDir(gradleProjectRootDirectory)
                .withArguments("coherencePof")
                .withDebug(true)
                .withPluginClasspath()
                .build();

        LOGGER.info(
                  "\n-------- [ Gradle output] -------->>>>\n"
                + gradleResult.getOutput()
                + "<<<<------------------------------------"
        );

        assertThat(gradleResult.getOutput()).contains("SUCCESS");
        assertThat(gradleResult.task(":coherencePof").getOutcome().name()).isEqualTo("SUCCESS");

        Class foo = getPofClass(this.gradleProjectRootDirectory, "Foo", "build/classes/java/main/");
        Class bar = getPofClass(this.gradleProjectRootDirectory, "Bar", "build/classes/java/test/");

        assertThatClassIsPofIntrumented(foo);
        assertThatClassIsPofIntrumented(bar);
        }

        @Test
        void applyCoherenceGradlePluginWithClassAndSchema()
        {

            final File buildFile = new File(gradleProjectRootDirectory, "build.gradle");

            appendToFile(buildFile,
                    """
                            plugins {
                              id 'java'
                              id 'com.oracle.coherence.gradle'
                            }
                            repositories {
                                mavenCentral()
                            }
                            dependencies {
                                implementation 'com.oracle.coherence.ce:coherence:22.09'
                            }
                            """
            );

            copyFileTo("/Foo.txt", gradleProjectRootDirectory,
                    "/src/main/java", "Foo.java");
            copyFileTo("/Bar.txt", gradleProjectRootDirectory,
                    "/src/main/java", "Bar.java");
            copyFileTo("/Color.txt", gradleProjectRootDirectory,
                    "/src/main/java", "Color.java");
            copyFileTo("/test-schema.xml", gradleProjectRootDirectory,
                    "/src/main/resources/META-INF", "schema.xml");

            BuildResult gradleResult = GradleRunner.create()
                    .withProjectDir(gradleProjectRootDirectory)
                    .withArguments("coherencePof")
                    .withDebug(true)
                    .withPluginClasspath()
                    .build();

            LOGGER.error(
                    "\n-------- [ Gradle output] -------->>>>\n"
                            + gradleResult.getOutput()
                            + "<<<<------------------------------------"
            );
            System.out.println(gradleResult.getOutput());
            assertThat(gradleResult.getOutput()).contains("SUCCESS");
            assertThat(gradleResult.task(":coherencePof").getOutcome().name()).isEqualTo("SUCCESS");

            Class foo = getPofClass(this.gradleProjectRootDirectory, "Foo", "build/classes/java/main/");
            assertThatClassIsPofIntrumented(foo);
        }

        @Test
        void applyCoherenceGradlePluginWithJarDependency()
        {

            final File buildFile = new File(gradleProjectRootDirectory, "build.gradle");

            appendToFile(buildFile,
                    """
                            plugins {
                              id 'java'
                              id 'com.oracle.coherence.gradle'
                            }
                            repositories {
                                mavenCentral()
                            }
                            dependencies {
                                implementation 'com.oracle.coherence.ce:coherence:22.09'
                                implementation files('lib/foo.jar')
                            }
                            coherencePof {
                                debug = true
                            }
                            """
            );

            copyFileTo("/foo.jar", gradleProjectRootDirectory,
                    "/lib", "foo.jar");
            copyFileTo("/Bar.txt", gradleProjectRootDirectory,
                    "/src/main/java", "Bar.java");

            BuildResult gradleResult = GradleRunner.create()
                    .withProjectDir(gradleProjectRootDirectory)
                    .withArguments("coherencePof")
                    .withDebug(true)
                    .withPluginClasspath()
                    .build();

            LOGGER.error(
                    "\n-------- [ Gradle output] -------->>>>\n"
                            + gradleResult.getOutput()
                            + "<<<<------------------------------------"
            );
            System.out.println(gradleResult.getOutput());
            assertThat(gradleResult.getOutput()).contains("SUCCESS");
            assertThat(gradleResult.task(":coherencePof").getOutcome().name()).isEqualTo("SUCCESS");

            Class foo = getPofClass(this.gradleProjectRootDirectory, "Foo", "build/classes/java/main/");
            assertThatClassIsPofIntrumented(foo);
        }
    }
