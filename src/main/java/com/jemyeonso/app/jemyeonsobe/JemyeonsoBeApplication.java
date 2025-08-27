package com.jemyeonso.app.jemyeonsobe;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@OpenAPIDefinition(
	info = @Info(
		title = "Jemyeonso Backend API",
		version = "v1.0",
		description = "Jemyeonso 서비스의 백엔드 API 명세입니다."
	)
)

@EnableAsync
@SpringBootApplication
public class JemyeonsoBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(JemyeonsoBeApplication.class, args);
	}

}
