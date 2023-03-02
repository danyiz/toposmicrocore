package account.management.configuration;

import com.gruelbox.transactionoutbox.SpringTransactionOutboxConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SpringTransactionOutboxConfiguration.class})
class ExternalConfig {}
