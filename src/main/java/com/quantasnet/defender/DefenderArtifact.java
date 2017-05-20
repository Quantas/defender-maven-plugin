package com.quantasnet.defender;

import java.util.Objects;

public class DefenderArtifact {
    private String groupId;
    private String artifactId;
    private String version;

    private String description;
    private String license;
    private String url;
    private String repository;

    private String scope;
    private boolean transitive;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isTransitive() {
        return transitive;
    }

    public void setTransitive(boolean transitive) {
        this.transitive = transitive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefenderArtifact that = (DefenderArtifact) o;
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(artifactId, that.artifactId) &&
                Objects.equals(version, that.version) &&
                Objects.equals(scope, that.scope) &&
                Objects.equals(transitive, that.transitive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, scope, transitive);
    }

    @Override
    public String toString() {
        return "Artifact[" + groupId + ':' + artifactId + ":" + version + ':' + scope + ']' + (transitive ? "-t" : "");
    }
}
