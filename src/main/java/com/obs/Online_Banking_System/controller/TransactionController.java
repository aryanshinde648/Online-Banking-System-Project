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
import com.obs.Online_Banking_System.service.AccountService;
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

    @GetMapping("/account")
    public ResponseEntity<?> getAccount(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Not authenticated");
            return ResponseEntity.status(401).body(err);
        }

        AccountResponseDto acc = accountService.getmyAccount(email);
        return ResponseEntity.ok(acc);
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

        // Derive the 6-digit transaction PIN from the last 6 digits of the Aadhar card
        String adharStr = String.valueOf(adharcard);
        String correctPin = adharStr.length() >= 6
                ? adharStr.substring(adharStr.length() - 6)
                : adharStr;

        if (request.getPin() == null || !correctPin.equals(request.getPin().trim())) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Invalid PIN. Please enter your 6-digit transaction PIN.");
            return ResponseEntity.status(401).body(err);
        }

        String msg = transactionService.transfer(request, email);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", msg);
        return ResponseEntity.ok(resp);
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

}
