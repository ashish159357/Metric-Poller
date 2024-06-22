package org.example.model;

public class CredentialProfile {

    private long credentialProfileId;
    private long credentialProfileName;
    private String username;
    private String password;

    public long getCredentialProfileId() {
        return credentialProfileId;
    }

    public void setCredentialProfileId(long credentialProfileId) {
        this.credentialProfileId = credentialProfileId;
    }

    public long getCredentialProfileName() {
        return credentialProfileName;
    }

    public void setCredentialProfileName(long credentialProfileName) {
        this.credentialProfileName = credentialProfileName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
