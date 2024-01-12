package antifraud.dto.request;

import antifraud.enums.TransactionAndFeedbackStatuses;
import antifraud.enums.ValueOfEnum;

import java.time.LocalDateTime;

public record TransactionFeedback(
        long transactionId,

        @ValueOfEnum(enumClass = TransactionAndFeedbackStatuses.class)
        String feedback) {
}
