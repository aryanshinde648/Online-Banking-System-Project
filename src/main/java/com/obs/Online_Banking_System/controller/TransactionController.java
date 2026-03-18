package com.obs.Online_Banking_System.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.obs.Online_Banking_System.dto.AccountResponseDto;
import com.obs.Online_Banking_System.dto.TransactionRequestDto;
import com.obs.Online_Banking_System.dto.TransactionResponseDto;
import com.obs.Online_Banking_System.entity.Customer;
import com.obs.Online_Banking_System.repository.CustomerRepository;
import com.obs.Online_Banking_System.service.AccountService;
import com.obs.Online_Banking_System.service.StatementService;
import com.obs.Online_Banking_System.service.TransactionService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/customer/api")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StatementService statementService;

    @GetMapping("/account")
    public ResponseEntity<?> getAccount(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Not authenticated");
            return ResponseEntity.status(401).body(err);
        }

        try {
            AccountResponseDto acc = accountService.getmyAccount(email);
            return ResponseEntity.ok(acc);
        } catch (RuntimeException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(404).body(err);
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransactionRequestDto request, HttpSession session) {
        String email = (String) session.getAttribute("email");
        Long adharcard = (Long) session.getAttribute("adharcard");

        if (email == null || adharcard == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Not authenticated");
            return ResponseEntity.status(401).body(err);
        }

        Customer cust = customerRepository.findByEmail(email).orElse(null);
        if (cust == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Customer not found");
            return ResponseEntity.status(404).body(err);
        }

        String correctPin = cust.getPin();

        if (request.getPin() == null || correctPin == null || !correctPin.equals(request.getPin().trim())) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Invalid PIN. Please enter your 6-digit transaction PIN.");
            return ResponseEntity.status(401).body(err);
        }

        try {
            String msg = transactionService.transfer(request, email);
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", msg);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody TransactionRequestDto request, HttpSession session) {
        String email = (String) session.getAttribute("email");
        Long adharcard = (Long) session.getAttribute("adharcard");

        if (email == null || adharcard == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Not authenticated");
            return ResponseEntity.status(401).body(err);
        }

        Customer cust = customerRepository.findByEmail(email).orElse(null);
        if (cust == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Customer not found");
            return ResponseEntity.status(404).body(err);
        }

        String correctPin = cust.getPin();
        if (request.getPin() == null || correctPin == null || !correctPin.equals(request.getPin().trim())) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Invalid PIN. Please enter your 6-digit transaction PIN.");
            return ResponseEntity.status(401).body(err);
        }

        try {
            String msg = transactionService.withdraw(request, email);
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", msg);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }


    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Not authenticated");
            return ResponseEntity.status(401).body(err);
        }

        List<TransactionResponseDto> txs = transactionService.getAllTransactions(email);
        return ResponseEntity.ok(txs);
    }

    /**
     * Download a PDF account statement.
     * Optional query params: from=YYYY-MM-DD, to=YYYY-MM-DD
     * Example: /customer/api/download-statement?from=2025-01-01&to=2025-03-05
     */
    @GetMapping("/download-statement")
    public void downloadStatement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpSession session,
            HttpServletResponse response) throws Exception {

        String email = (String) session.getAttribute("email");
        if (email == null) {
            response.sendError(401, "Not authenticated");
            return;
        }

        // Build a descriptive filename
        String filename;
        if (from != null && to != null) {
            filename = "statement_" + from + "_to_" + to + ".pdf";
        } else if (from != null) {
            filename = "statement_from_" + from + ".pdf";
        } else if (to != null) {
            filename = "statement_to_" + to + ".pdf";
        } else {
            filename = "account_statement.pdf";
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        transactionService.downloadStatement(email, from, to, response);
    }

    /**
     * POST /customer/api/send-statement
     * Sends the PDF statement to the customer's own registered email.
     * Optional params: from=YYYY-MM-DD, to=YYYY-MM-DD
     */
    @PostMapping("/send-statement")
    public ResponseEntity<?> sendStatement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpSession session) {

        String email = (String) session.getAttribute("email");
        if (email == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Not authenticated");
            return ResponseEntity.status(401).body(err);
        }

        try {
            statementService.sendStatementToEmail(email, email, from, to);
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "Statement sent to your registered email address.");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Failed to send statement: " + e.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }

}
