package com.engineerbetter;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jgit.api.Git;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SpringCloudConfigServerApplicationSmokeTest
{
	@Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();
	private RestTemplate restTemplate = new TestRestTemplate();


	@Before
	public void setup() throws Exception
	{
		File repoDir = tmpDir.newFolder("spring-cloud-config-repo");
		Git git = Git.init().setDirectory(repoDir).call();
		copyFixture("fixtures/application.properties", Paths.get(repoDir.getPath(), "application.properties"));
		git.commit().setAll(true).setMessage("Add properties").call();

		String arg = "--spring.cloud.config.server.git.uri=file://"+repoDir.getAbsolutePath();
		SpringApplication.run(SpringCloudConfigServerApplication.class, arg);
	}


	@Test
	public void returnsOk()
	{
		ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/app/default/", String.class);
		assertThat(response.getStatusCode(), is(HttpStatus.OK));
	}


	private void copyFixture(String fixture, Path destination) throws IOException
	{
		File fixtureFile = new File(getClass().getClassLoader().getResource(fixture).getFile());
		Files.copy(fixtureFile.toPath(), destination);
	}
}
