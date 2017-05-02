package com.quantasnet.defender;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mojo(name = "protect", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ProtectMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    private final RestTemplate restTemplate;

    public ProtectMojo() {
        restTemplate = new RestTemplate();

        final ObjectMapper objectMapper = new ObjectMapper();
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        final List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(converter);
        restTemplate.setMessageConverters(converters);
    }

    public void execute() throws MojoExecutionException {
        final Set<Artifact> artifacts = project.getArtifacts();

        final Set<DefenderArtifact> defenderArtifacts = new HashSet<>();

        artifacts.forEach(artifact -> {
            final DefenderArtifact defArtifact = new DefenderArtifact();
            defArtifact.setGroupId(artifact.getGroupId());
            defArtifact.setArtifactId(artifact.getArtifactId());
            defArtifact.setVersion(artifact.getVersion());
            defArtifact.setScope(artifact.getScope());
            defArtifact.setTransitive(artifact.getDependencyTrail().size() > 2);

            defenderArtifacts.add(defArtifact);
        });

        final DefenderArtifact app = new DefenderArtifact();
        app.setGroupId(project.getGroupId());
        app.setArtifactId(project.getArtifactId());
        app.setVersion(project.getVersion());

        final DefenderBuild build = new DefenderBuild();
        build.setUser(System.getProperty("user.name"));
        build.setApp(app);
        build.setArtifacts(defenderArtifacts);

        final HttpEntity<DefenderBuild> requestEntity = new HttpEntity<>(build);

        final ResponseEntity<Object> response = restTemplate.exchange("http://localhost:8080/api/protect", HttpMethod.POST, requestEntity, Object.class);
        if (response.hasBody()) {
            getLog().info("Response: " + response.getBody());
        } else {
            throw new MojoExecutionException("Build Failed");
        }

    }
}
