package team.tjusw.elm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("team.tjusw.elm.mapper")
@EnableFeignClients
public class Application {
	public static void main(String[] args)
	{
		SpringApplication.run(Application.class, args);
	}
}