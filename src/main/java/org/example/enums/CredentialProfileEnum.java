package org.example.enums;

public enum CredentialProfileEnum {

    INSERT_CREDENTIAL_PROFILE("INSERT INTO credential_profile (credentialProfileName,username,password) VALUES ($1,$2,$3)"),
    SELECT_CREDENTIAL_PROFILE("SELECT * from credential_profile where credentialprofileid=$1");

    private final String query;

    CredentialProfileEnum(String s) {
        query = s;
    }

    public String getQuery() {
        return query;
    }
}
