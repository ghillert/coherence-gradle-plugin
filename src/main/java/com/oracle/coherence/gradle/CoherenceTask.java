package com.oracle.coherence.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

abstract class CoherenceTask extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getFile1();
    @InputFile
    public abstract RegularFileProperty getFile2();
    @OutputFile
    public abstract RegularFileProperty getResultFile();

    @Inject
    public CoherenceTask(Project project) {
        getResultFile().convention(project.getLayout().getBuildDirectory().file("diff-result.txt")).get();
    }

    @TaskAction
    public void diff() {

        Map<Project, Set<Task>> tasks = this.getProject().getAllTasks(true);
        System.out.println(tasks.size());

//        Classpath classpath = getProject().getiles();
//        getProject()
//                .getTasksByName("compileJava", true)
//                .forEach(task -> classpath = Classpath.plus(((JavaCompile)task).getClasspath()));
//        classpath = Classpath.plus(getProject().getTasks().getByName("jar").getOutputs().getFiles());
//

        String diffResult;
        if (size(getFile1()) == size(getFile2())) {
            diffResult = String.format("Files have the same size at %s bytes.", getFile1().get().getAsFile().length());
        } else {
            File largestFile = size(getFile1()) > size(getFile2()) ? getFile1().get().getAsFile(): getFile2().get().getAsFile();
            diffResult = String.format("%s was the largest file at %s bytes.", largestFile.toString(), largestFile.length());
        }
        System.out.println("Result File: " + getResultFile().get().getAsFile());
        PluginUtils.appendString(getResultFile(), diffResult);

        System.out.println(String.format("File written to %s", getResultFile()));
        System.out.println(diffResult);
        System.out.println("Task Complete.");
    }

    private static long size(RegularFileProperty regularFileProperty) {
        return regularFileProperty.get().getAsFile().length();
    }
}
