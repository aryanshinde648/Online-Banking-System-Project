package com.obs.Online_Banking_System.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.obs.Online_Banking_System.dto.AccountCreateDto;
import com.obs.Online_Banking_System.dto.AccountDto;
import com.obs.Online_Banking_System.dto.TransactionDto;
import com.obs.Online_Banking_System.dto.TransactionRequestDto;
import com.obs.Online_Banking_System.dto.TransactionResponseDto;
import com.obs.Online_Banking_System.entity.Account;
import com.obs.Online_Banking_System.entity.Customer;
import com.obs.Online_Banking_System.entity.Transaction;
import com.obs.Online_Banking_System.enumDto.TransactionType;
import com.obs.Online_Banking_System.exception.ResourceNotFoundException;
import com.obs.Online_Banking_System.mapper.AccountConversion;
import com.obs.Online_Banking_System.mapper.TransactionConversion;
import com.obs.Online_Banking_System.repository.AccountRepository;
import com.obs.Online_Banking_System.repository.CustomerRepository;
import com.obs.Online_Banking_System.repository.TransactionRepository;
import com.obs.Online_Banking_System.service.TransactionService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

        @Autowired
        private TransactionRepository transactionRepository;

        @Autowired
        private CustomerRepository customerRepository;

        @Autowired
        private AccountRepository accountRepository;

        @Autowired
        private AccountConversion accountConversion;

        @Autowired
        private TransactionConversion trxConversion;

        @Override
        @Transactional
        public String deposit(TransactionRequestDto request, String email) {

                Customer cust = customerRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

                Account acc = accountRepository.findByAdharcard(cust.getAdharcard())
                                .orElseThrow(() -> new ResourceNotFoundException("Account not found for Customer"));

                BigDecimal amount = request.getAmount();

                acc.setBalance(acc.getBalance().add(amount));
                accountRepository.save(acc);

                Transaction tx = new Transaction();

                tx.setAccount(acc);
                tx.setAmount(amount);
                tx.setRemainingBalance(acc.getBalance());
                tx.setRemark(request.getRemark());
                tx.setTargetAccountNumber(request.getTargetAccountNo());
                tx.setTimestamp(Instant.now());
                tx.setTransactionType(TransactionType.DEPOSIT);
                tx.setSenderAccountId(acc.getId());
                tx.setReceiverAccountId(acc.getId());

                transactionRepository.save(tx);

                return "Amount deposited successfully";
        }

        @Override
        @Transactional
        public String withdraw(TransactionRequestDto request, String email) {

                Customer cust = customerRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

                Account acc = accountRepository.findByAdharcard(cust.getAdharcard())
                                .orElseThrow(() -> new ResourceNotFoundException("Account not found for Customer"));

                BigDecimal amount = request.getAmount();

                if (acc.getBalance().compareTo(amount) < 0) {
                        throw new RuntimeException("Insufficient balance");
                }

                acc.setBalance(acc.getBalance().subtract(amount));
                accountRepository.save(acc);

                Transaction tx = new Transaction();

                tx.setAccount(acc);
                tx.setAmount(amount);
                tx.setRemainingBalance(acc.getBalance());
                tx.setRemark(request.getRemark());
                tx.setTargetAccountNumber(request.getTargetAccountNo());
                tx.setTimestamp(Instant.now());
                tx.setTransactionType(TransactionType.WITHDRAW);
                tx.setSenderAccountId(acc.getId());
                tx.setReceiverAccountId(acc.getId());

                transactionRepository.save(tx);

                return "Amount withdrawn successfully";

        }

        @Override
        @Transactional
        public String transfer(TransactionRequestDto request, String email) {

                if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new RuntimeException("Invalid transfer amount");
                }

                Customer sender = customerRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Sender not found"));

                String senderAccNo = accountRepository
                                .findByAdharcard(sender.getAdharcard())
                                .orElseThrow(() -> new RuntimeException("Sender account not found"))
                                .getAccountNumber().toString();

                String receiverAccNo = request.getTargetAccountNo().toString();

                if (senderAccNo.equals(receiverAccNo)) {
                        throw new RuntimeException("Cannot transfer to same account");
                }

                // ✅ ALWAYS lock in sorted order (deadlock prevention)
                Account firstLock;
                Account secondLock;

                if (senderAccNo.compareTo(receiverAccNo) < 0) {
                        firstLock = accountRepository.findByAccountNumberForUpdate(senderAccNo)
                                        .orElseThrow(() -> new RuntimeException("Sender account not found"));

                        secondLock = accountRepository.findByAccountNumberForUpdate(receiverAccNo)
                                        .orElseThrow(() -> new RuntimeException("Receiver account not found"));
                } else {
                        firstLock = accountRepository.findByAccountNumberForUpdate(receiverAccNo)
                                        .orElseThrow(() -> new RuntimeException("Receiver account not found"));

                        secondLock = accountRepository.findByAccountNumberForUpdate(senderAccNo)
                                        .orElseThrow(() -> new RuntimeException("Sender account not found"));
                }

                // Assign sender/receiver directly from lock order — avoids Long.equals(String)
                // bug
                Account senderAccount, receiverAccount;
                if (senderAccNo.compareTo(receiverAccNo) < 0) {
                        // firstLock = sender, secondLock = receiver
                        senderAccount = firstLock;
                        receiverAccount = secondLock;
                } else {
                        // firstLock = receiver, secondLock = sender
                        receiverAccount = firstLock;
                        senderAccount = secondLock;
                }

                BigDecimal amount = request.getAmount();

                // ✅ balance check AFTER lock
                if (senderAccount.getBalance().compareTo(amount) < 0) {
                        throw new RuntimeException("Insufficient balance");
                }

                // ✅ perform transfer
                senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
                receiverAccount.setBalance(receiverAccount.getBalance().add(amount));

                accountRepository.save(senderAccount);
                accountRepository.save(receiverAccount);

                // Sender transaction (WITHDRAW)
                Transaction senderTx = new Transaction();
                senderTx.setAccount(senderAccount);
                senderTx.setAmount(amount);
                senderTx.setRemainingBalance(senderAccount.getBalance());
                senderTx.setRemark(request.getRemark());
                senderTx.setTargetAccountNumber(receiverAccount.getAccountNumber());
                senderTx.setTimestamp(Instant.now());
                senderTx.setTransactionType(TransactionType.TRANSFER);
                senderTx.setSenderAccountId(senderAccount.getId());
                senderTx.setReceiverAccountId(receiverAccount.getId());
                transactionRepository.save(senderTx);

                // Receiver transaction (DEPOSIT)
                Transaction receiverTx = new Transaction();
                receiverTx.setAccount(receiverAccount);
                receiverTx.setAmount(amount);
                receiverTx.setRemainingBalance(receiverAccount.getBalance());
                receiverTx.setRemark("Received from " + senderAccount.getAccountNumber());
                receiverTx.setTargetAccountNumber(senderAccount.getAccountNumber());
                receiverTx.setTimestamp(Instant.now());
                receiverTx.setTransactionType(TransactionType.TRANSFER);
                receiverTx.setSenderAccountId(senderAccount.getId());
                receiverTx.setReceiverAccountId(receiverAccount.getId());
                transactionRepository.save(receiverTx);

                log.info("Transfer from {} to {} amount {}",
                                senderAccNo, receiverAccNo, amount);

                return "Transfer successful";
        }

        @Override
        public List<TransactionResponseDto> getAllTransactions(String email) {

                Customer customer = customerRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Account account = accountRepository.findByCustomer(customer)
                                .orElseThrow(() -> new RuntimeException("Account not found"));

                List<TransactionResponseDto> txr = transactionRepository
                                .findByAccountOrderByTimestampDesc(account)
                                .stream()
                                .map(tx -> TransactionResponseDto.builder()
                                                .transactionId(tx.getId())
                                                .type(tx.getTransactionType().name())
                                                .amount(tx.getAmount().toString())
                                                .date(tx.getTimestamp().toString())
                                                .remark(tx.getRemark())
                                                .remainingBalance(tx.getRemainingBalance().toString())
                                                .targetAccount(tx.getTargetAccountNumber() == null ? null
                                                                : String.valueOf(tx.getTargetAccountNumber()))
                                                .senderAccountId(tx.getSenderAccountId())
                                                .receiverAccountId(tx.getReceiverAccountId())
                                                .senderName(resolveCustomerName(tx.getSenderAccountId(),
                                                                account.getId()))
                                                .receiverName(resolveCustomerName(tx.getReceiverAccountId(),
                                                                account.getId()))
                                                .build())
                                .toList();

                return txr;
        }

        /**
         * Resolve a JPA account ID to a customer full name.
         * Returns "Self" when the ID matches the logged-in customer's own account.
         * Returns "Unknown" when the account cannot be found.
         */
        private String resolveCustomerName(Long accountId, Long ownAccountId) {
                if (accountId == null)
                        return "—";
                if (accountId.equals(ownAccountId))
                        return "Self";
                return accountRepository.findById(accountId)
                                .map(acc -> {
                                        Customer c = acc.getCustomer();
                                        if (c == null)
                                                return "Unknown";
                                        String fname = c.getFname() != null ? c.getFname() : "";
                                        String lname = c.getLname() != null ? c.getLname() : "";
                                        return (fname + " " + lname).trim();
                                })
                                .orElse("Unknown");
        }

        @Override
        public String saveInitialDepositTransaction(AccountCreateDto acc, AccountDto accountDto, String email) {

                TransactionRequestDto tx = TransactionRequestDto
                                .builder()
                                .amount(BigDecimal.valueOf(acc.getInitialDeposit()))
                                .remark("Initial Deposit")
                                .targetAccountNo(accountDto.getAccountNumber())
                                .build();

                deposit(tx, email);

                return "Transaction saved";
        }

        @Override
        public Long getAllTransactionsCount() {
                return transactionRepository.count();
        }

        @Override
        public List<TransactionDto> findAllTransactions() {
                List<Transaction> txList = transactionRepository.findAll(
                Sort.by(Direction.DESC, "timestamp"));

                List<TransactionDto> trxDtoList = trxConversion.toTransactionDtoList(txList);

                return trxDtoList;
        }

}
