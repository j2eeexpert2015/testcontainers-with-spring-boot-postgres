package com.retailordersystem.constants;

/**
 * Defines Docker image names used across the application.
 * This class cannot be instantiated or extended.
 */
public final class DockerImageConstants {

    
    
    // MySQL Docker image
    public static final String MYSQL_IMAGE = "mysql:8.0";


    // Private constructor to prevent instantiation
    private DockerImageConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }
}
