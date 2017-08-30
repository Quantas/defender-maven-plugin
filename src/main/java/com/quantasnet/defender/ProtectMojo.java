package com.quantasnet.defender;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantasnet.defender.response.Build;
import com.quantasnet.defender.response.BuildDependency;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
@Mojo(name = "protect", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ProtectMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "false", property = "trust-self-signed")
    private String trustSelfSigned;

    @Override
    public void execute() throws MojoExecutionException {
        final DefenderBuild build = createDefenderBuild();
        submitBuild(build);
    }

    @SuppressWarnings("unchecked")
    private DefenderBuild createDefenderBuild() {
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
        app.setDescription(project.getDescription());

        final List<License> licenses = project.getLicenses();
        if (null != licenses && !licenses.isEmpty()) {
            app.setLicense(licenses.get(0).getName());
        }

        app.setUrl(project.getUrl());

        if (null != project.getScm()) {
            app.setRepository(project.getScm().getUrl());
        }

        final DefenderBuild build = new DefenderBuild();
        build.setUser(System.getProperty("user.name"));
        build.setApp(app);
        build.setArtifacts(defenderArtifacts);
        return build;
    }

    private void submitBuild(DefenderBuild build) throws MojoExecutionException {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("X-DEFENDER-TYPE", "MAVEN");

        final HttpEntity<DefenderBuild> requestEntity = new HttpEntity<>(build, headers);

        final ResponseEntity<Build> response = createRestTemplate().exchange("https://defender.quantasnet.net/api/protect", HttpMethod.POST, requestEntity, Build.class);
        if (response.hasBody()) {
            final Build buildResponse = response.getBody();

            if (!buildResponse.isPassed()) {
                // Build failed
                getLog().error("The build could not proceed due to the following dependencies:");

                for (BuildDependency buildDependency : buildResponse.getBuildDependencies()) {
                    if (!buildDependency.getDependencyStatus().isApproved()) {
                        getLog().error(buildDependency.getDependency().getGroupId() + ':' + buildDependency.getDependency().getArtifactId() + ':' + buildDependency.getDependency().getVersion() + " --- " + buildDependency.getDependencyStatus().getStatus());
                    }
                }
                throw new MojoExecutionException("DEFENDER PREVENTED BUILD DUE TO FAILURES!!!");
            }
        } else {
            throw new MojoExecutionException("Build Failed");
        }
    }

    private RestTemplate createRestTemplate() {

        if (Boolean.valueOf(trustSelfSigned)) {
            trustSelfSignedSSL();
        }

        final RestTemplate restTemplate = new RestTemplate();

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        final List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(converter);
        restTemplate.setMessageConverters(converters);

        return restTemplate;
    }

    private void trustSelfSignedSSL() {
        try {
            final SSLContext ctx = SSLContext.getInstance("TLS");

            final X509TrustManager tm = new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            ctx.init(null, new TrustManager[]{ tm }, null);
            SSLContext.setDefault(ctx);
        } catch (final Exception ex) {
            getLog().error(ex);
        }
    }
}
