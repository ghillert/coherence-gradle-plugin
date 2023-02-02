package com.oracle.coherence.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;

import java.util.Set;

public class CoherencePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create("fileDiff", CoherenceExtension.class);

        project.getTasks().register("fileDiff", CoherenceTask.class, fileDiffTask -> {


            //Same:  FileDiffExtension fileDiffExtension = (FileDiffExtension) project.getProperties().get("fileDiff");
            CoherenceExtension fileDiffExtension = project.getExtensions().getByType(CoherenceExtension.class);

            fileDiffTask.getFile1().value(fileDiffExtension.getFile1());
            fileDiffTask.getFile2().value(fileDiffExtension.getFile2());
        });
    }
}
