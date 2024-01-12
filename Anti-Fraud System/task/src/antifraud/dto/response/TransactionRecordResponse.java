package antifraud.dto.response;

import antifraud.enums.TransactionAndFeedbackStatuses;

public record TransactionRecordResponse(TransactionAndFeedbackStatuses result, String info) {
}
