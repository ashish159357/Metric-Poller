package org.example.model;

public class Discovery {
    private long discoveryId;
    private long credentialProfileId;
    private String name;
    private String ip;
    private String type;

    public long getDiscoveryId() {
        return discoveryId;
    }

    public long getCredentialProfileId() {
        return credentialProfileId;
    }

    public void setCredentialProfileId(long credentialProfileId) {
        this.credentialProfileId = credentialProfileId;
    }

    public void setDiscoveryId(long discoveryId) {
        this.discoveryId = discoveryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String hostname;
    private String protocol;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

}
