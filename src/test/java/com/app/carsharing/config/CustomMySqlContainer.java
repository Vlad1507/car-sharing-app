package com.app.carsharing.config;

import org.testcontainers.containers.MySQLContainer;

public class CustomMySqlContainer extends MySQLContainer<CustomMySqlContainer> {
    private static final String DB_IMAGE = "mysql:8";

    private static CustomMySqlContainer mySqlContainer;

    private CustomMySqlContainer() {
        super(DB_IMAGE);
    }

    public static synchronized CustomMySqlContainer getMySqlContainer() {
        if (mySqlContainer == null) {
            mySqlContainer = new CustomMySqlContainer();
        }
        return mySqlContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("spring.datasource.url", mySqlContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", mySqlContainer.getUsername());
        System.setProperty("spring.datasource.password", mySqlContainer.getPassword());
    }

    @Override
    public void stop() {
    }
}
