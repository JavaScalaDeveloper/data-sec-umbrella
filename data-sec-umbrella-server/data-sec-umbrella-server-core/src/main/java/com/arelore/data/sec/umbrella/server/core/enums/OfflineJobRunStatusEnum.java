package com.arelore.data.sec.umbrella.server.core.enums;

public enum OfflineJobRunStatusEnum {
    WAITING("waiting"),
    RUNNING("running"),
    FAILED("failed"),
    STOPPED("stopped"),
    COMPLETED("completed");

    private final String value;

    OfflineJobRunStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

