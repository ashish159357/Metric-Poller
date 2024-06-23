package org.example.enums;

public enum DiscoveryEnum {

    INSERT_DISCOVERY("INSERT INTO discovery (credential_id,type,hostname,protocol) VALUES ($1,$2,$3,$4)");

    private final String query;

    DiscoveryEnum(String s) {
        query = s;
    }

    public String getQuery() {
        return query;
    }
}
