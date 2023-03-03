/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.gradle;

import com.oracle.coherence.common.schema.ClassFileSchemaSource;
import com.oracle.coherence.common.schema.Schema;
import com.oracle.coherence.common.schema.SchemaBuilder;
import com.oracle.coherence.common.schema.XmlSchemaSource;
import com.tangosol.io.pof.generator.PortableTypeGenerator;
import com.tangosol.io.pof.schema.annotation.PortableType;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.Directory;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.oracle.coherence.common.schema.ClassFileSchemaSource.Filters.hasAnnotation;

/**
 * @author Gunnar Hillert
 */
abstract class CoherenceTask extends DefaultTask
    {

    @Input
    @Optional
    public abstract Property<Boolean> getDebug();

    @Input
    @Optional
    public abstract Property<Boolean> getInstrumentTestClasses();

    /**
     * Set the project test classes directory.
     **/
     @InputFiles
     @Optional
     abstract Property<File> getTestClassesDirectory();

     @InputFiles
     @Optional
     abstract Property<File> getTestResourcesDirectory();

    @InputFiles
    @Optional
    abstract Property<File> getMainClassesDirectory();

    @InputFiles
    @Optional
    abstract Property<File> getMainResourcesDirectory();

    @Inject
    public CoherenceTask(Project project)
        {

        getLogger().info("Setting up Task property conventions.");
        getDebug().convention(false);
        getInstrumentTestClasses().convention(false);

        final Directory mainJavaOutputDir = PluginUtils.getMainJavaOutputDir(project);
        getMainClassesDirectory().convention(mainJavaOutputDir.getAsFile());

        final Directory testJavaOutputDir = PluginUtils.getTestJavaOutputDir(project);
        getTestClassesDirectory().convention(testJavaOutputDir.getAsFile());

        final File mainResourcesOutputDir = PluginUtils.getMainResourcesOutputDir(project);
        if (mainResourcesOutputDir != null)
            {
            getMainResourcesDirectory().convention(mainResourcesOutputDir);
            }

        final File testResourcesOutputDir = PluginUtils.getTestResourcesOutputDir(project);
        if (testResourcesOutputDir != null)
            {
            getTestResourcesDirectory().convention(testResourcesOutputDir);
            }

        }

    @TaskAction
    public void instrumentPofClasses()
        {

        getLogger().lifecycle("Start executing Gradle task instrumentPofClasses...");
        getLogger().info("The following configuration properties are configured:");
        getLogger().info("Property debug = {}", this.getDebug().get());
        getLogger().info("Property instrumentTestClasses = {}", this.getInstrumentTestClasses().get());
        getLogger().info("Property testClassesDirectory = {}", this.getTestClassesDirectory());
        getLogger().info("Property mainClassesDirectory = {}", this.getMainClassesDirectory());

        ClassFileSchemaSource source = new ClassFileSchemaSource();
        List<File> listInstrument = new ArrayList<>();
        SchemaBuilder schemaBuilder = new SchemaBuilder();

        List<File> classesDirectories = new ArrayList<>();

        addSchemaSourceIfExists(schemaBuilder, getTestResourcesDirectory());
        addSchemaSourceIfExists(schemaBuilder, getMainResourcesDirectory());

        if (getTestClassesDirectory().isPresent()
            && getTestClassesDirectory().get().exists())
            {
            File testClassesDirectoryAsFile = getTestClassesDirectory().get();
            classesDirectories.add(testClassesDirectoryAsFile);
            }
        else
            {
            getLogger().error("PortableTypeGenerator skipping test classes directory as it does not exist.");
            }

        if (getMainClassesDirectory().isPresent()
                && getMainClassesDirectory().get().exists())
            {
            File mainClassesDirectoryAsFile = getMainClassesDirectory().get();
            classesDirectories.add(mainClassesDirectoryAsFile);
            }
        else
            {
            getLogger().error("PortableTypeGenerator skipping main classes directory as it does not exist.");
            }

        if (!classesDirectories.isEmpty()) {
            source.withTypeFilter(hasAnnotation(PortableType.class))
                  .withMissingPropertiesAsObject();
            for (File classesDir : classesDirectories)
                {
                source.withClassesFromDirectory(classesDir);
                listInstrument.add(classesDir);
                }
        }

        if (!listInstrument.isEmpty())
            {
            List<File> listDeps = resolveDependencies();
            ClassFileSchemaSource dependencies =
                    new ClassFileSchemaSource()
                            .withTypeFilter(hasAnnotation(PortableType.class))
                            .withPropertyFilter(fieldNode -> false);

            listDeps.stream()
                    .filter(File::isDirectory)
                    .peek(f -> getLogger().lifecycle("Adding classes from " + f + " to schema"))
                    .forEach(dependencies::withClassesFromDirectory);

            listDeps.stream()
                    .filter(f -> f.isFile() && f.getName().endsWith(".jar"))
                    .peek(f -> getLogger().lifecycle("Adding classes from " + f + " to schema"))
                    .forEach(dependencies::withClassesFromJarFile);

            Schema schema = schemaBuilder
                    .addSchemaSource(dependencies)
                    .addSchemaSource(source)
                    .build();

            for (File dir : listInstrument)
                {
                try
                    {
                    getLogger().warn("Running PortableTypeGenerator for classes in " + dir.getCanonicalPath());
                    PortableTypeGenerator.instrumentClasses(dir, schema, this.getDebug().get(), new GradleLogger(getLogger()));
                    }
                catch (IOException e)
                    {
                    throw new RuntimeException(e);
                    }
                }
            }
        }

        private void addSchemaSourceIfExists(SchemaBuilder builder, Property<File> resourcesDirectory)
            {
                if (resourcesDirectory.isPresent())
                    {
                        File resourcesDirectoryAsFile = resourcesDirectory.get();

                        if (resourcesDirectoryAsFile.exists())
                            {
                                File xmlSchema = Paths.get(resourcesDirectoryAsFile.getPath(), "META-INF", "schema.xml").toFile();
                                if (xmlSchema.exists())
                                    {
                                    getLogger().lifecycle("Add XmlSchemaSource '{}'.", xmlSchema.getAbsolutePath());
                                    builder.addSchemaSource(new XmlSchemaSource(xmlSchema));
                                    }
                                else
                                    {
                                    getLogger().info("No schema.xml file found at {}", xmlSchema.getAbsolutePath());
                                    }
                        }
                        else {
                            getLogger().info("The specified resources directory '{}' does not exist.");
                        }
                    }
                else
                    {
                        getLogger().info("The resources directory property is not present.");
                    }
            }

    private List<File> resolveDependencies() //throws ArtifactResolverException
        {
        List<File> listArtifacts = new ArrayList<>();

        Configuration configuration = this.getProject().getConfigurations().getByName("runtimeClasspath"); // TODO May need to be configurable
        configuration.forEach(file -> {
            getLogger().info("Adding dependency '{}'.", file.getAbsolutePath());
            if (file.exists())
                {
                listArtifacts.add(file);
                }
            else
                {
                getLogger().info("Dependency '{}' does not exist.", file.getAbsolutePath());
                }

        });
        getLogger().lifecycle("Resolved {} dependencies.", listArtifacts.size());
        return listArtifacts;
        }
    }
