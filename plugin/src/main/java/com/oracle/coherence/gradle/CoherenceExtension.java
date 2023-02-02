/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

/**
 * @author Gunnar Hillert
 */
public abstract class CoherenceExtension
    {
    public abstract Property<Boolean> getDebug();

    public abstract Property<Boolean> getInstrumentTestClasses();

    abstract DirectoryProperty getTestClassesDirectory();

    abstract DirectoryProperty getMainClassesDirectory();
    }
