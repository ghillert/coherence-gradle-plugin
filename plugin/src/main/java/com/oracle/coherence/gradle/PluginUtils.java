/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.gradle;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;

/**
 * @author Gunnar Hillert
 */
public final class PluginUtils
    {
    private PluginUtils()
        {
        throw new AssertionError("This is a static utility class.");
        }

    static int getGradleMajorVersion(Project project)
        {
        String gradleVersion = project.getGradle().getGradleVersion();
        return Integer.parseInt(gradleVersion.substring(0, gradleVersion.indexOf(".")));
        }

    static Directory getJavaMainOutputDir(Project project)
        {
        JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        SourceSet sourceSet = javaPluginExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        Directory classesDir = sourceSet.getJava().getClassesDirectory().get();
        project.getLogger().warn("Java Main output directory: {}.", classesDir.getAsFile().getAbsolutePath());
        return classesDir;
        }

    static Directory getJavaTestOutputDir(Project project)
        {
        JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        SourceSet sourceSet = javaPluginExtension.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

        Directory classesDir = sourceSet.getJava().getClassesDirectory().get();
        project.getLogger().warn("Java Test Output directory: {}.", classesDir.getAsFile().getAbsolutePath());
        return classesDir;
        }
    }
