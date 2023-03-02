package account.management.configuration;

import com.gruelbox.transactionoutbox.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ConfigOutBox {

    @Bean
    @Lazy
    public TransactionOutbox transactionOutbox(SpringTransactionManager springTransactionManager,
                                               SpringInstantiator springInstantiator) {
        return TransactionOutbox.builder()
                .instantiator(springInstantiator)
                .transactionManager(springTransactionManager)
                .persistor(Persistor.forDialect(Dialect.POSTGRESQL_9))
                .build();
    }
}
