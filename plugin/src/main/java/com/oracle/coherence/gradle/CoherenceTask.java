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
    abstract Property<File> getMainClassesDirectory();

    @Inject
    public CoherenceTask(Project project)
        {

        getDebug().convention(false);
        getInstrumentTestClasses().convention(false);

        final Directory javaMainOutputDir = PluginUtils.getJavaMainOutputDir(project);
        getMainClassesDirectory().convention(javaMainOutputDir.getAsFile());

        final Directory javaTestOutputDir = PluginUtils.getJavaTestOutputDir(project);
        getTestClassesDirectory().convention(javaTestOutputDir.getAsFile());

        }

    @TaskAction
    public void instrumentPofClasses()
        {

        this.getProject().getLogger().info("Start executing Gradle task instrumentPofClasses...");
        this.getProject().getLogger().info("The following configuration properties are configured:");
        this.getProject().getLogger().info("Property debug = {}", this.getDebug().get());
        this.getProject().getLogger().info("Property instrumentTestClasses = {}", this.getInstrumentTestClasses().get());
        this.getProject().getLogger().info("Property testClassesDirectory = {}", this.getTestClassesDirectory());
        this.getProject().getLogger().info("Property mainClassesDirectory = {}", this.getMainClassesDirectory());

        ClassFileSchemaSource source = new ClassFileSchemaSource();
        List<File> listInstrument = new ArrayList<>();
        SchemaBuilder builder = new SchemaBuilder();

        if (getMainClassesDirectory().isPresent()
            && getMainClassesDirectory().get().exists())
            {
            File mainClassesDirectoryAsFile = getMainClassesDirectory().get();
            source.withClassesFromDirectory(mainClassesDirectoryAsFile)
                    .withTypeFilter(hasAnnotation(PortableType.class))
                    .withMissingPropertiesAsObject();

            File xmlSchema = Paths.get(mainClassesDirectoryAsFile.getPath(), "META-INF", "schema.xml").toFile();
            if (xmlSchema.exists())
                {
                builder.addSchemaSource(new XmlSchemaSource(xmlSchema));
                }

            listInstrument.add(mainClassesDirectoryAsFile);
            }
        else
            {
            this.getProject().getLogger().error("PortableTypeGenerator skipping main classes directory as it does not exist.");
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
                    .peek(f -> getLogger().debug("Adding classes from " + f + " to schema"))
                    .forEach(dependencies::withClassesFromDirectory);

            listDeps.stream()
                    .filter(f -> f.isFile() && f.getName().endsWith(".jar"))
                    .peek(f -> getLogger().debug("Adding classes from " + f + " to schema"))
                    .forEach(dependencies::withClassesFromJarFile);

            Schema schema = builder
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
    private List<File> resolveDependencies() //throws ArtifactResolverException
        {
        List<File> listArtifacts = new ArrayList<>();
        Logger log        = this.getProject().getLogger();

            //TODO
//        for (Artifact artifact : m_project.getArtifacts())
//        {
//            if (artifact.getScope().equals(Artifact.SCOPE_TEST) && !f_fTests)
//            {
//                continue;
//            }
//
//            String sArtifactId = artifact.getArtifactId();
//
//            if (!artifact.isResolved())
//            {
//                log.debug("Resolving artifact " + artifact);
//
//                ProjectBuildingRequest req = new DefaultProjectBuildingRequest()
//                        .setRepositorySession(m_session)
//                        .setLocalRepository(m_localRepository)
//                        .setRemoteRepositories(m_listRemoteRepositories);
//
//                ArtifactResult result = m_artifactResolver.resolveArtifact(req, artifact);
//                artifact = result.getArtifact();
//            }
//
//            // The file should exists, but we never know.
//            File file = artifact.getFile();
//            if (file == null || !file.exists())
//            {
//                log.warn("Artifact " + sArtifactId
//                        + " has no attached file. Its content will not be copied to the target model directory.");
//            }
//            else
//            {
//                log.debug("Adding file: artifact=" + artifact + " file=" + file);
//
//                listArtifacts.add(file);
//            }
//        }

        return listArtifacts;
        }
    }
