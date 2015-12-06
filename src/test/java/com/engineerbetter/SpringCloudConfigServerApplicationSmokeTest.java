package com.engineerbetter;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jgit.api.Git;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.engineerbetter.fixtures.SpringCloudConfigClientApplication;

public class SpringCloudConfigServerApplicationSmokeTest
{
	@Rule
	public TemporaryFolder tmpDir = new TemporaryFolder();
	private RestTemplate restTemplate = new TestRestTemplate();

	private ConfigurableApplicationContext serverContext;
	private ConfigurableApplicationContext clientContext;


	@Before
	public void setup() throws Exception
	{
		File repoDir = setupGitRepo();

		serverContext = SpringApplication.run(
			SpringCloudConfigServerApplication.class,
			"--spring.cloud.config.server.git.uri=file://"+repoDir.getAbsolutePath()
		);
	}


	@After
	public void teardown()
	{
		serverContext.close();

		if(clientContext != null)
		{
			clientContext.close();
		}
	}


	@Test
	public void returnsOk()
	{
		ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/app/default/", String.class);
		assertThat(response.getStatusCode(), is(HttpStatus.OK));
	}


	@Test
	public void clientGetsValueFromServer()
	{
		startClient(true);
		ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8081/", String.class);
		assertThat(response.getBody(), is("test-value"));
	}


	@Test
	public void clientGetsDefaultValueWhenCloudConfigDisabled()
	{
		startClient(false);
		ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8081/", String.class);
		assertThat(response.getBody(), is("default-value"));
	}


	@Test
	public void clientGetsDefaultValueWhenCloudConfigServerUnreachable()
	{
		serverContext.close();
		startClient(false);
		ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8081/", String.class);
		assertThat(response.getBody(), is("default-value"));
	}


	private File setupGitRepo() throws Exception
	{
		File repoDir = tmpDir.newFolder("spring-cloud-config-repo");
		Git git = Git.init().setDirectory(repoDir).call();
		copyFixture("fixtures/example.properties", Paths.get(repoDir.getPath(), "application.properties"));
		git.add().addFilepattern(".").call();
		git.commit().setMessage("Add properties").call();
		return repoDir;
	}


	private void copyFixture(String fixture, Path destination) throws IOException
	{
		File fixtureFile = new File(getClass().getClassLoader().getResource(fixture).getFile());
		Files.copy(fixtureFile.toPath(), destination);
	}


	private void startClient(boolean configEnabled)
	{
		clientContext = SpringApplication.run(
			SpringCloudConfigClientApplication.class,
			"--spring.jmx.enabled=false",
			"--spring.profiles.active=client",
			"--spring.cloud.config.enabled="+configEnabled,
			"--spring.cloud.config.uri=http://localhost:8080",
			"--my.key=default-value",
			"--server.port=8081"
		);
	}
}
