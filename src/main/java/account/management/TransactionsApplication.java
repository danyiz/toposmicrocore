package account.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.module.paranamer.ParanamerModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

@SpringBootApplication
@Slf4j
@EnableAutoConfiguration
public class TransactionsApplication implements ApplicationRunner, ApplicationListener {

	@Autowired
	private ObjectMapper objectMapper;


	public static void main(String[] args) {
		SpringApplication.run(TransactionsApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments arg0) throws Exception {

	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		SimpleModule module = new SimpleModule();
		module.addSerializer(java.sql.Date.class, new DateSerializer());
		objectMapper.registerModule(new ParanamerModule());
		objectMapper.registerModule(module);

	}
}
