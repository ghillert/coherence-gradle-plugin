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

import java.io.File;

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

    static Directory getMainJavaOutputDir(Project project)
        {
        JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        SourceSet sourceSet = javaPluginExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        Directory classesDir = sourceSet.getJava().getClassesDirectory().getOrNull();

        if (classesDir == null)
            {
            project.getLogger().warn("Main Java output directory not available.");
            }
        else
            {
            project.getLogger().warn("Main Java output directory: {}.", classesDir.getAsFile().getAbsolutePath());
            }
        return classesDir;
        }

    static File getMainResourcesOutputDir(Project project)
        {
        JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        SourceSet sourceSet = javaPluginExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        File classesDir = sourceSet.getOutput().getResourcesDir();

        if (classesDir == null)
            {
            project.getLogger().warn("Main Resources output directory not available.");
            }
        else
            {
            project.getLogger().warn("Main Resources output directory: {}.", classesDir.getAbsolutePath());
            }

        return classesDir;
        }

    static Directory getTestJavaOutputDir(Project project)
        {
        JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        SourceSet sourceSet = javaPluginExtension.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

        Directory classesDir = sourceSet.getJava().getClassesDirectory().getOrNull();

        if (classesDir == null)
            {
            project.getLogger().warn("Test Java output directory not available.");
            }
        else
            {
            project.getLogger().warn("Test Java output directory: {}.", classesDir.getAsFile().getAbsolutePath());
            }

        return classesDir;
        }

    static File getTestResourcesOutputDir(Project project)
        {
        JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        SourceSet sourceSet = javaPluginExtension.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);

        File classesDir = sourceSet.getOutput().getResourcesDir();
        if (classesDir == null)
        {
            project.getLogger().warn("Test Resources output directory not available.");
        }
        else
        {
            project.getLogger().warn("Test Resources output directory: {}.", classesDir.getAbsolutePath());
        }

        return classesDir;
        }
    }


