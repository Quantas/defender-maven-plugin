package com.quantasnet.defender;

import java.util.Set;

public class DefenderBuild {

    private String user;
    private DefenderArtifact app;
    private Set<DefenderArtifact> artifacts;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public DefenderArtifact getApp() {
        return app;
    }

    public void setApp(DefenderArtifact app) {
        this.app = app;
    }

    public Set<DefenderArtifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Set<DefenderArtifact> artifacts) {
        this.artifacts = artifacts;
    }
}
