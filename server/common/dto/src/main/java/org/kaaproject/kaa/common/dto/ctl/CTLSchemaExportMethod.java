package org.kaaproject.kaa.common.dto.ctl;

/**
 * A method of CTL schema export.
 * 
 * @author Bohdan Khablenko
 * 
 * @since v0.8.0
 */
public enum CTLSchemaExportMethod {

    /**
     * Return the body of a CTL schema as a file.
     */
    SHALLOW,

    /**
     * Return the body of a CTL schema with all dependencies inline as a file,
     * recursively.
     */
    FLAT,

    /**
     * Return the body of a CTL schema as a file and all dependencies as
     * different files, recursively.
     */
    DEEP;
}
