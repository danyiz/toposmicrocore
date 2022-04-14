package account.management;

import account.management.entity.AccountBalances;
import account.management.entity.TransactionBalances;
import account.management.entity.AccountAttributes;
import account.management.repository.AccountBalancesRepository;
import account.management.repository.TransactionBalancesRepository;
import account.management.repository.AccountAttributesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.kafka.annotation.EnableKafka;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EnableKafka
@SpringBootApplication
@Slf4j
@EnableAutoConfiguration(exclude = LiquibaseAutoConfiguration.class)
public class TransactionsApplication implements ApplicationRunner {

	@Autowired
	AccountBalancesRepository accountBalancesRepository;

	@Autowired
	AccountAttributesRepository accountAttributesRepository;

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
			log.info("Transaction_blances : {}", transactionBalances.toString());

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
			log.info("Transaction_blances : {}", transactionBalances1.toString());


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
			log.info("Transaction_blances : {}", transactionBalances2.toString());

		}
		catch (Exception e)
		{
			  log.info(e.getMessage());
		}

	}

}
