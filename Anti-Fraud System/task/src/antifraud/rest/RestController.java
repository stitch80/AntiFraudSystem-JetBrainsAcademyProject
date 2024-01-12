package antifraud.rest;

import antifraud.data.AppUserRepository;
import antifraud.data.IPAddressRepository;
import antifraud.data.StolenCardRepository;
import antifraud.data.TransactionRepository;
import antifraud.dto.request.AppUserOperation;
import antifraud.dto.request.AppUserRole;
import antifraud.dto.request.TransactionFeedback;
import antifraud.dto.response.*;
import antifraud.entity.AppUser;
import antifraud.entity.IP;
import antifraud.entity.StolenCard;
import antifraud.entity.Transaction;
import antifraud.enums.TransactionAndFeedbackStatuses;
import antifraud.rest.exceptions.ObjectConflictException;
import antifraud.rest.exceptions.ObjectNotFoundException;
import antifraud.rest.exceptions.ObjectNotValidException;
import antifraud.rest.exceptions.UnprocessableEntityException;
import antifraud.tools.CardNumberChecker;
import antifraud.tools.TransactionChecker;
import antifraud.tools.TransactionLimitChanger;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

//import org.apache.commons.validator.routines.CreditCardValidator;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api")
public class RestController {

    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository userRepository;
    private final IPAddressRepository ipAddressRepository;
    private final StolenCardRepository stolenCardRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionChecker transactionChecker;

    private final TransactionLimitChanger transactionLimitChanger;

//    private final CreditCardValidator creditCardValidator;



    public RestController(
            PasswordEncoder passwordEncoder,
            AppUserRepository userRepository,
            IPAddressRepository ipAddressRepository,
            StolenCardRepository stolenCardRepository,
            TransactionRepository transactionRepository,
            TransactionChecker transactionChecker,
            TransactionLimitChanger transactionLimitChanger) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.ipAddressRepository = ipAddressRepository;
        this.stolenCardRepository = stolenCardRepository;
        this.transactionRepository = transactionRepository;
        this.transactionChecker = transactionChecker;
        this.transactionLimitChanger = transactionLimitChanger;
//        this.creditCardValidator = new CreditCardValidator();
    }


// region <Auth>

    @PostMapping("/auth/user")
    @ResponseStatus(HttpStatus.CREATED)
    public AppUserResponse registerNewAppUser(
            @Valid @RequestBody AppUser appUser
    ) {

        Optional<AppUser> foundAppUser = userRepository.findByUsername(appUser.getUsername());
        if (foundAppUser.isPresent()) {
            throw new ObjectConflictException();
        }

//        AppUser newUser = new AppUser();
//        newUser.setName(appUser.name());
        appUser.setUsername(appUser.getUsername().toLowerCase());
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        if (userRepository.count() == 0) {
            appUser.setRole(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR"));
            appUser.setNonLocked(true);
        } else {
            appUser.setRole(new SimpleGrantedAuthority("ROLE_MERCHANT"));        }

        AppUser savedUser = userRepository.save(appUser);
        return new AppUserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getUsername(),
                savedUser.getRoleName());
    }


    @GetMapping("/auth/list")
    public Iterable<AppUserResponse> getAppUsers() {
        Iterable<AppUser> users = userRepository.findAllByOrderById();
        List<AppUserResponse> output = new ArrayList<>();
        for (AppUser au : users) {
            output.add(new AppUserResponse(
                    au.getId(),
                    au.getName(),
                    au.getUsername(),
                    au.getRoleName()));
        }
        return output;
    }

    @DeleteMapping("/auth/user/{username}")
    public AppUserDeleteResponse deleteUserByName(@PathVariable String username) {
        if (userRepository.findByUsername(username).isEmpty()) {
            throw new ObjectNotFoundException();
        }
        userRepository.deleteByUsername(username);
        return new AppUserDeleteResponse(username, "Deleted successfully!");
    }

    @PutMapping("/auth/role")
    private AppUserResponse updateAppUsersRole(
            @RequestBody AppUserRole userRole
    ) {
        if (!userRole.role().equals("SUPPORT") &&
        !userRole.role().equals("MERCHANT")) {
            throw new ObjectNotValidException();
        }
        AppUser user = userRepository.findByUsername(userRole.username())
                .orElseThrow(ObjectNotFoundException::new);
        if (userRole.role().equals(user.getRoleName())) {
            throw new ObjectConflictException();
        }
        user.setRole(new SimpleGrantedAuthority("ROLE_" + userRole.role()));
        AppUser savedUser = userRepository.save(user);
        return new AppUserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getUsername(),
                savedUser.getRoleName()
        );
    }

    @PutMapping("/auth/access")
    public AppUserOperationResponse updateAppUserStatus(
            @RequestBody AppUserOperation operation
    ) {
        AppUser user = userRepository.findByUsername(operation.username())
                .orElseThrow(ObjectNotFoundException::new);
        if(operation.operation().equals("LOCK")) {
            user.setNonLocked(false);
        } else if (operation.operation().equals("UNLOCK")) {
            user.setNonLocked(true);
        } else {
            throw new ObjectConflictException();
        }

        AppUser updatedAppUser = userRepository.save(user);
        StringBuilder statusMessage = new StringBuilder("User ");
        statusMessage.append(updatedAppUser.getUsername());
        statusMessage.append(" ");
        if (updatedAppUser.isNonLocked()) {
            statusMessage.append("unlocked");
        } else {
            statusMessage.append("locked");
        }
        statusMessage.append("!");

        return new AppUserOperationResponse(statusMessage.toString());
    }

    //endregion

    //region <Antifraud>
    @PostMapping("/antifraud/suspicious-ip")
    public IP saveIPAddress(
            @Valid @RequestBody IP ip
            ) {
        return ipAddressRepository.save(ip);
    }

    @DeleteMapping("/antifraud/suspicious-ip/{ip}")
    public ObjectDeleteResponse deleteIPAddress(@PathVariable String ip) {
        if (!ip.matches("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$")) {
            throw new ObjectNotValidException();
        }
        if (ipAddressRepository.findByIp(ip).isEmpty()) {
            throw new ObjectNotFoundException();
        }
        ipAddressRepository.deleteByIp(ip);
        return new ObjectDeleteResponse("IP " + ip + " successfully removed!");
    }

    @GetMapping("/antifraud/suspicious-ip")
    public Iterable<IP> getIPAddresses() {
        return ipAddressRepository.findAllByOrderById();
    }

    @PostMapping("/antifraud/stolencard")
    public StolenCard addStolenCard(
            @Valid @RequestBody StolenCard card
    ) {
        return stolenCardRepository.save(card);
    }

    @DeleteMapping("/antifraud/stolencard/{number}")
    public ObjectDeleteResponse deleteStolenCard(@PathVariable String number) {

        if (!CardNumberChecker.isValid(number)) {
            throw new ObjectNotValidException();
        }

//        if (!creditCardValidator.isValid(number)) {
//            throw new ObjectNotValidException();
//        }
        if (stolenCardRepository.findByNumber(number).isEmpty()) {
            throw new ObjectNotFoundException();
        }
        stolenCardRepository.deleteByNumber(number);
        return new ObjectDeleteResponse("Card " + number + " successfully removed!");
    }

    @GetMapping("/antifraud/stolencard")
    public Iterable<StolenCard> getStolenCards() {
        return stolenCardRepository.findAllByOrderByNumber();
    }


    @PostMapping("/antifraud/transaction")
    public TransactionRecordResponse simpleValidationEndPoint(
            @Valid @RequestBody Transaction transaction
    ) {

        TransactionAndFeedbackStatuses result = transactionChecker.getResult(transaction);
        String output;
        if (result.equals(TransactionAndFeedbackStatuses.ALLOWED)) {
            output = "none";
        } else {
            output = transactionChecker.getInfo(transaction, result);
        }

        transaction.setResult(result.toString());
        transactionRepository.save(transaction);

        return new TransactionRecordResponse(result, output);

    }

    @GetMapping("/antifraud/history")
    public Iterable<Transaction> getTransactions() {
        return transactionRepository.findAll(Sort.by("id").ascending());
    }

    @PutMapping("/antifraud/transaction")
    public Transaction updateTransactionFeedback(@Valid @RequestBody TransactionFeedback feedback) {
        Transaction transaction = transactionRepository.findById(feedback.transactionId())
                .orElseThrow(ObjectNotFoundException::new);
        if (!transaction.getFeedback().isEmpty()) {
            throw new ObjectConflictException();
        }

        String feedbackValue = feedback.feedback();
        String resultValue = transaction.getResult();

        if (resultValue.equals(feedbackValue)) {
            throw new UnprocessableEntityException();
        }


        String feedbackStatusAllowed = TransactionAndFeedbackStatuses.ALLOWED.toString();
        String feedbackStatusManualProcessing = TransactionAndFeedbackStatuses.MANUAL_PROCESSING.toString();
        String feedbackStatusProhibited = TransactionAndFeedbackStatuses.PROHIBITED.toString();
        if (resultValue.equals(feedbackStatusAllowed)) {
            if (feedbackValue.equals(feedbackStatusManualProcessing)) {
                transactionLimitChanger.decreaseLimit("ALLOWED", transaction);
            } else if (feedbackValue.equals(feedbackStatusProhibited)) {
                transactionLimitChanger.decreaseLimit("ALLOWED", transaction);
                transactionLimitChanger.decreaseLimit("MANUAL", transaction);
            }
        } else if (resultValue.equals(feedbackStatusManualProcessing)) {
            if (feedbackValue.equals(feedbackStatusAllowed)) {
                transactionLimitChanger.increaseLimit("ALLOWED", transaction);
            } else if (feedbackValue.equals(feedbackStatusProhibited)) {
                transactionLimitChanger.decreaseLimit("MANUAL", transaction);
            }
        } else if (resultValue.equals(feedbackStatusProhibited)) {
            if (feedbackValue.equals(feedbackStatusAllowed)) {
                transactionLimitChanger.increaseLimit("ALLOWED", transaction);
                transactionLimitChanger.increaseLimit("MANUAL", transaction);
            } else if (feedbackValue.equals(feedbackStatusManualProcessing)) {
                transactionLimitChanger.increaseLimit("MANUAL", transaction);
            }
        }

        transaction.setFeedback(feedbackValue);
        return transactionRepository.save(transaction);
    }

    @GetMapping("/antifraud/history/{number}")
    private Iterable<Transaction> getTransactionsByCard(@PathVariable String number) {
        if (!CardNumberChecker.isValid(number)) {
            throw new ObjectNotValidException();
        }

//        if (!creditCardValidator.isValid(number)) {
//            throw new ObjectNotValidException();
//        }

        Iterable<Transaction> transactions = transactionRepository.findAllByNumber(number);
        long transactionsListSize = StreamSupport.stream(transactions.spliterator(), false).count();


        if (transactionsListSize > 0) {
            return transactions;
        } else {
            throw new ObjectNotFoundException();
        }
    }


    //endregion

}
