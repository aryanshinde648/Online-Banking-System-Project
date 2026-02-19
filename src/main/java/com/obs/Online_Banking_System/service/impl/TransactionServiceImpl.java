package com.obs.Online_Banking_System.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.obs.Online_Banking_System.dto.TransactionRequestDto;
import com.obs.Online_Banking_System.dto.TransactionResponseDto;
import com.obs.Online_Banking_System.entity.Account;
import com.obs.Online_Banking_System.entity.Customer;
import com.obs.Online_Banking_System.entity.Transaction;
import com.obs.Online_Banking_System.enumDto.TransactionType;
import com.obs.Online_Banking_System.exception.ResourceNotFoundException;
import com.obs.Online_Banking_System.repository.AccountRepository;
import com.obs.Online_Banking_System.repository.CustomerRepository;
import com.obs.Online_Banking_System.repository.TransactionRepository;
import com.obs.Online_Banking_System.service.TransactionService;

@Service
public class TransactionServiceImpl implements TransactionService {

        @Autowired
        private TransactionRepository transactionRepository;

        @Autowired
        private CustomerRepository customerRepository;

        @Autowired
        private AccountRepository accountRepository;

        @Override
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

                transactionRepository.save(tx);

                return "Amount deposited successfully";
        }

        @Override
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

                Account senderAccount = firstLock.getAccountNumber().equals(senderAccNo) ? firstLock : secondLock;
                Account receiverAccount = firstLock.getAccountNumber().equals(receiverAccNo) ? firstLock : secondLock;

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

                // (keep your transaction saving code here)

                return "Transfer successful";
        }

        @Override
        public List<TransactionResponseDto> getAllTransactions(String email) {

                Customer customer = customerRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Account account = accountRepository.findByCustomer(customer)
                                .orElseThrow(() -> new RuntimeException("Account not found"));

                List<TransactionResponseDto> txr = transactionRepository.findByAccount(account)
                                .stream()
                                .map(tx -> TransactionResponseDto.builder()
                                                .type(tx.getTransactionType().name())
                                                .amount(tx.getAmount().toString())
                                                .date(tx.getTimestamp().toString())
                                                .remark(tx.getRemark())
                                                .remainingBalance(tx.getRemainingBalance().toString())
                                                .targetAccount(tx.getTargetAccountNumber() == null ? null
                                                                : String.valueOf(tx.getTargetAccountNumber()))
                                                .build())
                                .toList();

                return txr;
        }

}
