package antifraud.tools;

import antifraud.data.IPAddressRepository;
import antifraud.data.StolenCardRepository;
import antifraud.data.CardLimitRepository;
import antifraud.data.TransactionRepository;
import antifraud.entity.CardLimit;
import antifraud.entity.Transaction;
import antifraud.enums.TransactionAndFeedbackStatuses;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@Component
public class TransactionChecker {

    private final IPAddressRepository ipAddressRepository;
    private final StolenCardRepository stolenCardRepository;
    private final TransactionRepository transactionRepository;
    private final CardLimitRepository cardLimitRepository;

    public TransactionChecker(
            IPAddressRepository ipAddressRepository,
            StolenCardRepository stolenCardRepository,
            TransactionRepository transactionRepository,
            CardLimitRepository cardLimitRepository) {
        this.ipAddressRepository = ipAddressRepository;
        this.stolenCardRepository = stolenCardRepository;
        this.transactionRepository = transactionRepository;
        this.cardLimitRepository = cardLimitRepository;
    }

    public TransactionAndFeedbackStatuses getResult(Transaction transaction) {
        //Check prohibited ip-addresses and stolen cards
        if (
                ipAddressRepository.existsByIp(transaction.getIp())
                        || stolenCardRepository.existsByNumber(transaction.getNumber())
        ) {
            return TransactionAndFeedbackStatuses.PROHIBITED;
        }

        //Check ip-address and regions correlations
        int numberOfIpAddresses = getNumberOfIpAddresses(transaction);
        ArrayList<String> ipAddresses = getIpAddresses(transaction);

        int numberOfRegions = getNumberOfRegions(transaction);
        ArrayList<String> regions = getRegions(transaction);

        if ((numberOfIpAddresses == 2 && !ipAddresses.contains(transaction.getIp()))
                || (numberOfRegions == 2 && !regions.contains(transaction.getRegion()))) {
            return TransactionAndFeedbackStatuses.MANUAL_PROCESSING;
        } else if ((numberOfIpAddresses > 2 && !ipAddresses.contains(transaction.getIp()))
                || (numberOfRegions > 2 && !regions.contains(transaction.getRegion()))) {
            return TransactionAndFeedbackStatuses.PROHIBITED;
        }

        //Check amount
        CardLimit cardLimit = getCardLimit(transaction);

        if (transaction.getAmount() <= cardLimit.getAllowLimit()) {
            return TransactionAndFeedbackStatuses.ALLOWED;
        } else if (transaction.getAmount() <= cardLimit.getManualLimit()) {
            return TransactionAndFeedbackStatuses.MANUAL_PROCESSING;
        } else {
            return TransactionAndFeedbackStatuses.PROHIBITED;
        }
    }

    public String getInfo(Transaction transaction, TransactionAndFeedbackStatuses result) {
        ArrayList<String> list = new ArrayList<>();
        if (ipAddressRepository.existsByIp(transaction.getIp()))
            list.add("ip");
        if (stolenCardRepository.existsByNumber(transaction.getNumber()))
            list.add("card-number");
        int numberOfIpAddresses = getNumberOfIpAddresses(transaction);
        int numberOfRegions = getNumberOfRegions(transaction);
        CardLimit cardLimit = getCardLimit(transaction);
        if (result.equals(TransactionAndFeedbackStatuses.MANUAL_PROCESSING)) {
            if (transaction.getAmount() > cardLimit.getAllowLimit())
                list.add("amount");
            if (numberOfIpAddresses == 2)
                list.add("ip-correlation");
            if (numberOfRegions == 2)
                list.add("region-correlation");
        }
        if (result.equals(TransactionAndFeedbackStatuses.PROHIBITED)) {
            if (transaction.getAmount() > cardLimit.getManualLimit())
                list.add("amount");
            if (numberOfIpAddresses > 2)
                list.add("ip-correlation");
            if (numberOfRegions > 2)
                list.add("region-correlation");
        }
        Collections.sort(list);
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            output.append(list.get(i));
            if (i < list.size() - 1)
                output.append(", ");
        }
        return output.toString();
    }

    private CardLimit getCardLimit(Transaction transaction) {
        Optional<CardLimit> cardLimitOptional = cardLimitRepository.findByCardNumber(transaction.getNumber());
        CardLimit cardLimit = new CardLimit();
        if (cardLimitOptional.isEmpty()) {
            cardLimit = new CardLimit(transaction.getNumber(), CardLimit.ALLOW_LIMIT, CardLimit.MANUAL_LIMIT);
            cardLimitRepository.save(cardLimit);
        } else {
            cardLimit = cardLimitOptional.get();
        }
        return cardLimit;
    }

    private int getNumberOfIpAddresses(Transaction transaction) {
        return transactionRepository.countIpAddressesInLastHour(
                transaction.getNumber(),
                transaction.getDate().minusHours(1),
                transaction.getDate());
    }

    private int getNumberOfRegions(Transaction transaction) {
        return transactionRepository.countRegionsInLastHour(
                transaction.getNumber(),
                transaction.getDate().minusHours(1),
                transaction.getDate());
    }

    private ArrayList<String> getRegions(Transaction transaction) {
        Iterable<String> regionsIterable = transactionRepository.getRegionsInLastHour(
                transaction.getNumber(),
                transaction.getDate().minusHours(1),
                transaction.getDate()
        );
        ArrayList<String> regions = new ArrayList<>();
        regionsIterable.forEach(regions::add);
        return regions;
    }

    private ArrayList<String> getIpAddresses(Transaction transaction) {
        Iterable<String> ipAddressesIterable = transactionRepository.getIpAddressesInLastHour(
                transaction.getNumber(),
                transaction.getDate().minusHours(1),
                transaction.getDate()
        );
        ArrayList<String> ipAddresses = new ArrayList<>();
        ipAddressesIterable.forEach(ipAddresses::add);
        return ipAddresses;
    }
}
