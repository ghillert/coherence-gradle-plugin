/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.gradle;

import com.tangosol.io.pof.PortableTypeSerializer;
import com.tangosol.io.pof.SimplePofContext;
import com.tangosol.io.pof.annotation.Portable;
import com.tangosol.io.pof.annotation.PortableProperty;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

        assertThat(gradleResult.getOutput()).contains("PortableTypeGenerator skipping test classes directory as it does not exist.");
        assertThat(gradleResult.getOutput()).contains("PortableTypeGenerator skipping main classes directory as it does not exist.");
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
        assertThat(gradleResult.getOutput()).contains("Instrumenting type Foo");

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

        copyFileTo("/Color.txt", gradleProjectRootDirectory,
                "/src/test/java", "Color.java");

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

            LOGGER.info(
                    "\n-------- [ Gradle output] -------->>>>\n"
                  + gradleResult.getOutput()
                  + "<<<<------------------------------------"
            );

            assertThat(gradleResult.getOutput()).contains("SUCCESS");
            assertThat(gradleResult.task(":coherencePof").getOutcome().name()).isEqualTo("SUCCESS");

            assertThat(gradleResult.getOutput()).contains("Add XmlSchemaSource", "build/resources/main/META-INF/schema.xml");
            assertThat(gradleResult.getOutput()).contains("Instrumenting type Bar");
            assertThat(gradleResult.getOutput()).contains("Instrumenting type Foo");
            assertThat(gradleResult.getOutput()).contains("SUCCESS");

            Class foo = getPofClass(this.gradleProjectRootDirectory, "Foo", "build/classes/java/main/");
            assertThatClassIsPofIntrumented(foo);

            Class bar = getPofClass(this.gradleProjectRootDirectory, "Bar", "build/classes/java/main/");
            assertThatClassIsPofIntrumented(bar);
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

            LOGGER.info(
                    "\n-------- [ Gradle output] -------->>>>\n"
                  + gradleResult.getOutput()
                  + "<<<<------------------------------------"
            );
            assertThat(gradleResult.getOutput()).contains("SUCCESS");
            assertThat(gradleResult.task(":coherencePof").getOutcome().name()).isEqualTo("SUCCESS");
            assertThat(gradleResult.getOutput()).contains("foo.jar to schema");

        }

        @Test
        void verifyCoherenceGradlePluginWithRoundTripSerialization() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
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
            copyFileTo("/Person.txt", gradleProjectRootDirectory,
                    "/src/main/java", "Person.java");
//
//            copyFileTo("/Bar.txt", gradleProjectRootDirectory,
//                    "/src/main/java", "Bar.java");
//
//            copyFileTo("/Color.txt", gradleProjectRootDirectory,
//                    "/src/main/java", "Color.java");
//
//            copyFileTo("/test-schema.xml", gradleProjectRootDirectory,
//                    "/src/main/resources/META-INF", "schema.xml");

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

            Class personClass = getPofClass(this.gradleProjectRootDirectory, "Person", "build/classes/java/main/");
            assertThatClassIsPofIntrumented(personClass);
            Class addressClass = personClass.getClasses()[0];
            SimplePofContext ctx = new SimplePofContext();

            ctx.registerUserType(1000, personClass, new PortableTypeSerializer<>(1000, personClass));
            ctx.registerUserType(2, addressClass, new PortableTypeSerializer(2, addressClass));

            Constructor<?> constructor = personClass.getDeclaredConstructor(String.class, String.class, int.class);
            Object         oValue      = constructor.newInstance("Eric", "Cartman", 10);

            Constructor<?> addressConstructor = addressClass.getDeclaredConstructor(String.class, String.class, String.class);
            Object         addressInstance = addressConstructor.newInstance("123 Main St", "Springfield", "USA");

            Method setAddressMethod = personClass.getMethod("setAddress", addressClass);
            setAddressMethod.invoke(oValue, addressInstance);
            Binary binary              = ExternalizableHelper.toBinary(oValue, ctx);
            Object         oResult     = ExternalizableHelper.fromBinary(binary, ctx);

            assertThat(oResult).isEqualTo(oValue);

//            Class barClass = getPofClass(this.gradleProjectRootDirectory, "Bar", "build/classes/java/main/");
//            Class colorClass = getPofClass(this.gradleProjectRootDirectory, "Color", "build/classes/java/main/");
//            assertThatClassIsPofIntrumented(barClass);
//
//            SimplePofContext ctx = new SimplePofContext();
//
//            ctx.registerUserType(1001, barClass, new PortableTypeSerializer<>(1001, barClass));
//            ctx.registerUserType(1002, colorClass, new PortableTypeSerializer<>(1002, colorClass));
//
//            Constructor<?> constructor = barClass.getDeclaredConstructor(String.class, String.class);
//            Object         oValue      = constructor.newInstance("Bockbier", "BLACK");
//            Binary binary              = ExternalizableHelper.toBinary(oValue, ctx);
//            Object         oResult     = ExternalizableHelper.fromBinary(binary, ctx);
//
//            assertThat(oResult).isEqualTo(oValue);
        }
    }
