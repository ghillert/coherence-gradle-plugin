package com.oracle.coherence.gradle;

import org.gradle.api.file.RegularFileProperty;

public abstract class CoherenceExtension {
    public abstract RegularFileProperty getFile1();
    public abstract RegularFileProperty getFile2();
}
