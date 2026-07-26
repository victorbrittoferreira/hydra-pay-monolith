package io.hydra.monolith.application.usecase;

public interface ProcessPaymentUseCase {
    ProcessPaymentResult handle(ProcessPaymentCommand command);
}
