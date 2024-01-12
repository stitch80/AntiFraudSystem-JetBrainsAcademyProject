package antifraud.tools;

import antifraud.data.CardLimitRepository;
import antifraud.entity.CardLimit;
import antifraud.entity.Transaction;
import antifraud.rest.exceptions.ObjectNotFoundException;
import antifraud.rest.exceptions.ObjectNotValidException;
import org.springframework.stereotype.Component;

@Component
public class TransactionLimitChanger {

    private final CardLimitRepository cardLimitRepository;

    public TransactionLimitChanger(CardLimitRepository cardLimitRepository) {
        this.cardLimitRepository = cardLimitRepository;
    }

    public void increaseLimit(String limitName, Transaction transaction) {
        CardLimit cardLimit = cardLimitRepository.findByCardNumber(transaction.getNumber())
                .orElseThrow(ObjectNotFoundException::new);
        if (limitName.equals("ALLOWED")) {
            int currentAllowLimit = cardLimit.getAllowLimit();
            int newLimit = getNewHigherLimit(transaction, currentAllowLimit);
            cardLimit.setAllowLimit(newLimit);
        } else if (limitName.equals("MANUAL")) {
            int currentManualLimit = cardLimit.getManualLimit();
            int newLimit = getNewHigherLimit(transaction, currentManualLimit);
            cardLimit.setManualLimit(newLimit);
        } else {
            throw new ObjectNotValidException();
        }
        cardLimitRepository.save(cardLimit);
    }

    public void decreaseLimit(String limitName, Transaction transaction) {
        CardLimit cardLimit = cardLimitRepository.findByCardNumber(transaction.getNumber())
                .orElseThrow(ObjectNotFoundException::new);
        if (limitName.equals("ALLOWED")) {
            int currentAllowLimit = cardLimit.getAllowLimit();
            int newLimit = getNewLowerLimit(transaction, currentAllowLimit);
            cardLimit.setAllowLimit(newLimit);
        } else if (limitName.equals("MANUAL")) {
            int currentManualLimit = cardLimit.getManualLimit();
            int newLimit = getNewLowerLimit(transaction, currentManualLimit);
            cardLimit.setManualLimit(newLimit);
        } else {
            throw new ObjectNotValidException();
        }
        cardLimitRepository.save(cardLimit);
    }

    private static int getNewHigherLimit(Transaction transaction, int currentLimit) {
        int transactionAmount = transaction.getAmount();
        int newLimit = (int) Math.ceil(0.8 * currentLimit + 0.2 * transactionAmount);
        return newLimit;
    }

    private static int getNewLowerLimit(Transaction transaction, int currentLimit) {
        int transactionAmount = transaction.getAmount();
        int newLimit = (int) Math.ceil(0.8 * currentLimit - 0.2 * transactionAmount);
        return newLimit;
    }
}
