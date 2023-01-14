package account.management;

import account.management.repository.TransactionBalances;
import account.management.repository.TransactionBalancesRepository;
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
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@SpringBootApplication
@Slf4j
@EnableAutoConfiguration
@EnableJpaRepositories
@EntityScan("account.management.repository")
public class TransactionsApplication implements ApplicationRunner, ApplicationListener {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	TransactionBalancesRepository transactionBalancesRepository;

	public static void main(String[] args) {
		SpringApplication.run(TransactionsApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments arg0) throws Exception {

		try {
			TransactionBalances transactionBalances = new TransactionBalances();
			transactionBalances.setSchemaCode("LOAN1");
			transactionBalances.setTransactionGroup("PAYMENT");
			Map<String, Integer> balances = new HashMap<>() ;
			balances.put("BOOK_BALANCE",1);
			balances.put("VALUE_BALANCE",1);
			balances.put("OVERPAYMENT",1);
			transactionBalances.setBalanceComponents(balances);
			transactionBalancesRepository.saveAndFlush(transactionBalances);
			log.info("Transaction_balances : {}", transactionBalances);

			TransactionBalances transactionBalances1 = new TransactionBalances();
			transactionBalances1.setSchemaCode("LOAN1");
			transactionBalances1.setTransactionGroup("INTEREST");
			balances.clear();
			balances.put("BOOK_BALANCE",1);
			balances.put("VALUE_BALANCE",1);
			balances.put("DUE_INTEREST",1);
			balances.put("TOTAL_INTEREST",1);
			transactionBalances1.setBalanceComponents(balances);
			transactionBalancesRepository.saveAndFlush(transactionBalances1);
			log.info("Transaction_balances : {}", transactionBalances1);


			TransactionBalances transactionBalances2 = new TransactionBalances();
			transactionBalances2.setSchemaCode("LOAN1");
			transactionBalances2.setTransactionGroup("PENALTY_INTEREST");
			balances.clear();
			balances.put("BOOK_BALANCE",1);
			balances.put("VALUE_BALANCE",1);
			balances.put("DUE_PENALTY_INTEREST",1);
			balances.put("TOTAL_PENALTY_INTEREST",1);
			transactionBalances2.setBalanceComponents(balances);
			transactionBalancesRepository.saveAndFlush(transactionBalances2);
			log.info("Transaction_balances : {}", transactionBalances2);

		}
		catch (Exception e)
		{
			  log.info(e.getMessage());
		}

	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		SimpleModule module = new SimpleModule();
		module.addSerializer(java.sql.Date.class, new DateSerializer());
		objectMapper.registerModule(new ParanamerModule());
		objectMapper.registerModule(module);

	}
}
