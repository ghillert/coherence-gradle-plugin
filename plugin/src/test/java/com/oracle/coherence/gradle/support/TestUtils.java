/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.gradle.support;

import com.tangosol.io.pof.schema.annotation.internal.Instrumented;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gunnar Hillert
 */
public class TestUtils
    {
    public static void appendToFile(File buildFile, String textToAppend)
        {
        try (FileWriter out = new FileWriter(buildFile, true))
            {
            out.write(textToAppend);
            }
        catch (IOException ex)
            {
            throw new RuntimeException(
                    String.format("Something went wring while appending %s to file %s",
                            textToAppend, buildFile.getAbsoluteFile()), ex);
            }
        }

    public static void copyFileTo(String sourceFileName, File root, String destinationDir, String destinationFilename)
        {
        File javaDir = new File(root, destinationDir);
        javaDir.mkdirs();
        URL inputUrl = TestUtils.class.getResource(sourceFileName);
        try
            {
            FileUtils.copyURLToFile(inputUrl, new File(javaDir, destinationFilename));
            }
        catch (IOException e)
            {
            throw new RuntimeException(e);
            }
        }
    public static Class getPofClass(File gradleProjectRootDirectory, String classname, String baseDirectory)
        {

        File classDirectory = new File(gradleProjectRootDirectory, baseDirectory);

        assertThat(classDirectory.exists())
                .withFailMessage("classDirectory %s does not exist.", classDirectory.getAbsolutePath())
                .isTrue();
        assertThat(classDirectory.isDirectory()).isTrue();

        final URL url;
        try
            {
            url = classDirectory.toURI().toURL();
            }
        catch (MalformedURLException e)
            {
            throw new RuntimeException(e);
            }
        URL[] urls = new URL[]{url};

        ClassLoader classLoader = new URLClassLoader(urls);

        try
            {
            return classLoader.loadClass(classname);
            }
        catch (ClassNotFoundException e)
            {
            throw new RuntimeException(e);
            }
        }

    public static void assertThatClassIsPofIntrumented(Class pofClass)
        {

        assertThat(pofClass).isNotNull();

        Annotation[] annotations = pofClass.getAnnotations();

        assertThat(annotations.length).withFailMessage("Class '%s' should have 2 annotations,", pofClass.getName()).isEqualTo(2);
        assertThat(pofClass.getAnnotation(Instrumented.class)).isNotNull();
        assertThat(pofClass.getInterfaces().length).isEqualTo(2);

        }
}
