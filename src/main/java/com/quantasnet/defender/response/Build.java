package com.quantasnet.defender.response;

import java.util.Set;

public class Build {
    private String version;

    private String userName;

    private boolean passed;

    private Set<BuildDependency> buildDependencies;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public Set<BuildDependency> getBuildDependencies() {
        return buildDependencies;
    }

    public void setBuildDependencies(Set<BuildDependency> buildDependencies) {
        this.buildDependencies = buildDependencies;
    }
}
