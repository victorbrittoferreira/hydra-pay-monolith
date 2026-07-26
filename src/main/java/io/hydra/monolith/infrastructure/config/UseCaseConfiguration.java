package io.hydra.monolith.infrastructure.config;

import io.hydra.monolith.application.port.AccountRepository;
import io.hydra.monolith.application.port.LedgerRepository;
import io.hydra.monolith.application.port.TransactionRepository;
import io.hydra.monolith.application.service.ProcessPaymentService;
import io.hydra.monolith.application.usecase.ProcessPaymentResult;
import io.hydra.monolith.application.usecase.ProcessPaymentUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class UseCaseConfiguration {

    @Bean
    public ProcessPaymentUseCase processPaymentUseCase(
            TransactionTemplate transactionTemplate,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            LedgerRepository ledgerRepository
    ) {
        ProcessPaymentService service = new ProcessPaymentService(
                accountRepository,
                transactionRepository,
                ledgerRepository
        );
        return command -> transactionTemplate.execute(status -> {
            ProcessPaymentResult result = service.handle(command);
            if (result == null) {
                throw new IllegalStateException("Payment processing returned null");
            }
            return result;
        });
    }
}
