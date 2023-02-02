/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.gradle;

import com.tangosol.io.pof.generator.PortableTypeGenerator;
import org.gradle.api.logging.Logger;

/**
 * @author Gunnar Hillert
 */
public class GradleLogger implements PortableTypeGenerator.Logger
    {
    /**
     * Create a logger that wraps the specified Maven logger.
     *
     * @param logger  the Maven logger
     */
    public GradleLogger(Logger logger)
        {
        this.logger = logger;
        }

    // ----- PortableTypeGenerator.Logger methods -----------------------

    @Override
    public void debug(String message)
        {
        this.logger.warn(message);
        }

    @Override
    public void info(String message)
        {
        this.logger.warn(message);
        }

    // ----- data members -----------------------------------------------

    /**
     * The wrapped Maven logger.
     */
    private final Logger logger;
}
