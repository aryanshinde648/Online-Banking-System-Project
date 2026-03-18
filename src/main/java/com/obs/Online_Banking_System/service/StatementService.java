package com.obs.Online_Banking_System.service;

import java.time.LocalDate;

public interface StatementService {

    /**
     * Generates a PDF statement and returns it as bytes.
     * @param email    customer email (owner of account)
     * @param from     optional start date filter
     * @param to       optional end date filter
     */
    byte[] generateStatementBytes(String email, LocalDate from, LocalDate to) throws Exception;

    /**
     * Generates a PDF statement and sends it to the given email address.
     * @param ownerEmail   the account owner's email (used to fetch account/transactions)
     * @param toEmail      email address to send the statement to
     * @param from         optional date range start
     * @param to           optional date range end
     */
    void sendStatementToEmail(String ownerEmail, String toEmail, LocalDate from, LocalDate to) throws Exception;
}
