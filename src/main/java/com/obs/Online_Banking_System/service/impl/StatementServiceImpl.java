package com.obs.Online_Banking_System.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.obs.Online_Banking_System.entity.Account;
import com.obs.Online_Banking_System.entity.Customer;
import com.obs.Online_Banking_System.entity.Transaction;
import com.obs.Online_Banking_System.repository.AccountRepository;
import com.obs.Online_Banking_System.repository.CustomerRepository;
import com.obs.Online_Banking_System.repository.TransactionRepository;
import com.obs.Online_Banking_System.service.EmailService;
import com.obs.Online_Banking_System.service.StatementService;
import com.obs.Online_Banking_System.util.PdfStatementGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final CustomerRepository    customerRepository;
    private final AccountRepository     accountRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService          emailService;

    @Override
    public byte[] generateStatementBytes(String email, LocalDate from, LocalDate to) throws Exception {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + email));

        Account account = accountRepository.findByCustomer(customer)
                .orElseThrow(() -> new RuntimeException("Account not found for customer: " + email));

        List<Transaction> transactions = transactionRepository
                .findByAccountOrderByTimestampDesc(account);

        log.info("[Statement] Generating PDF for {} ({} transactions)", email, transactions.size());

        return PdfStatementGenerator.generateToBytes(transactions, account, customer, from, to);
    }

    @Override
    public void sendStatementToEmail(String ownerEmail, String toEmail,
                                     LocalDate from, LocalDate to) throws Exception {

        Customer customer = customerRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + ownerEmail));

        Account account = accountRepository.findByCustomer(customer)
                .orElseThrow(() -> new RuntimeException("Account not found for: " + ownerEmail));

        List<Transaction> transactions = transactionRepository
                .findByAccountOrderByTimestampDesc(account);

        // Generate PDF bytes
        byte[] pdfBytes = PdfStatementGenerator.generateToBytes(transactions, account, customer, from, to);

        // Build a descriptive file name
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM_yyyy"));
        String firstName = customer.getFname() != null ? customer.getFname().replaceAll("\\s+", "") : "Customer";
        String fileName = "Statement_" + firstName + "_" + month + ".pdf";

        // Build recipient-friendly body text
        String fullName = trim(customer.getFname()) + " " + trim(customer.getLname());
        String accNum = String.valueOf(account.getAccountNumber());
        String body = "Dear " + fullName + ",<br><br>"
                + "Please find attached your bank statement for account number <strong>" + accNum + "</strong>.<br><br>"
                + "If you did not request this, please contact our support team immediately.";

        String subject = "📄 Your Bank Statement — Online Banking System";

        emailService.sendEmailWithAttachment(toEmail, subject, body, pdfBytes, fileName);

        log.info("[Statement] Emailed PDF '{}' to {} for account {}", fileName, toEmail, accNum);
    }

    private String trim(String s) {
        return (s != null) ? s.trim() : "";
    }
}
