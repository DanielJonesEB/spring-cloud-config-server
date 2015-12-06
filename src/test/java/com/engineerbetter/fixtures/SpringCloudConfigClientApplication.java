package com.engineerbetter.fixtures;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@Profile("client")
public class SpringCloudConfigClientApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(SpringCloudConfigClientApplication.class, args);
	}


	@RestController
	@Profile("client")
	public static class ClientController
	{
		@Value("${my.key}")
		private String configurationValue;


		@RequestMapping("/")
		public String getValue()
		{
			return configurationValue;
		}
	}
}
