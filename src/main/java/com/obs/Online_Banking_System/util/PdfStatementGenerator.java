package com.obs.Online_Banking_System.util;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import com.obs.Online_Banking_System.entity.Account;
import com.obs.Online_Banking_System.entity.Customer;
import com.obs.Online_Banking_System.entity.Transaction;
import com.obs.Online_Banking_System.enumDto.TransactionType;

import jakarta.servlet.http.HttpServletResponse;

public class PdfStatementGenerator {

    // ── Brand colours ──────────────────────────────────────────────────────────
    private static final Color BRAND_DARK = new Color(28, 40, 65); // deep navy
    private static final Color BRAND_PURPLE = new Color(102, 126, 234); // accent
    private static final Color HEADER_BG = new Color(28, 40, 65);
    private static final Color TABLE_HEADER = new Color(44, 62, 80);
    private static final Color ROW_ALT = new Color(248, 250, 255);
    private static final Color CREDIT_GREEN = new Color(22, 163, 74);
    private static final Color DEBIT_RED = new Color(220, 38, 38);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy")
            .withZone(ZoneId.systemDefault());

    // ── Public entry-point (HttpServletResponse) ───────────────────────────────
    public static void generate(
            List<Transaction> transactions,
            Account account,
            Customer customer,
            LocalDate from,
            LocalDate to,
            HttpServletResponse response) throws Exception {

        List<Transaction> filtered = filterByDate(transactions, from, to);

        Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, response.getOutputStream());
        writer.setPageEvent(new FooterEvent());
        doc.open();

        addHeader(doc, writer, from, to);
        doc.add(Chunk.NEWLINE);
        addAccountInfo(doc, account, customer);
        doc.add(Chunk.NEWLINE);
        addTransactionTable(doc, filtered);
        doc.add(Chunk.NEWLINE);
        addSummary(doc, filtered, account.getBalance());

        doc.close();
    }

    // ── Public entry-point (byte[] — for email attachments) ────────────────────
    public static byte[] generateToBytes(
            List<Transaction> transactions,
            Account account,
            Customer customer,
            LocalDate from,
            LocalDate to) throws Exception {

        List<Transaction> filtered = filterByDate(transactions, from, to);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        writer.setPageEvent(new FooterEvent());
        doc.open();

        addHeader(doc, writer, from, to);
        doc.add(Chunk.NEWLINE);
        addAccountInfo(doc, account, customer);
        doc.add(Chunk.NEWLINE);
        addTransactionTable(doc, filtered);
        doc.add(Chunk.NEWLINE);
        addSummary(doc, filtered, account.getBalance());

        doc.close();
        return baos.toByteArray();
    }

    // ── Date filtering helper ──────────────────────────────────────────────────
    private static List<Transaction> filterByDate(List<Transaction> transactions,
            LocalDate from, LocalDate to) {
        if (from == null && to == null) return transactions;
        return transactions.stream().filter(t -> {
            LocalDate txDate = t.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate();
            boolean afterFrom = (from == null) || !txDate.isBefore(from);
            boolean beforeTo  = (to   == null) || !txDate.isAfter(to);
            return afterFrom && beforeTo;
        }).toList();
    }

    // ── Section 1 — Header banner ──────────────────────────────────────────────
    private static void addHeader(Document doc, PdfWriter writer,
            LocalDate from, LocalDate to) throws DocumentException {

        // Full-width coloured rectangle drawn directly on the canvas
        PdfContentByte cb = writer.getDirectContentUnder();
        cb.saveState();
        cb.setColorFill(HEADER_BG);
        // iText rectangle(x, y, width, height) uses bottom-left corner.
        // Page is A4: 595.0f wide by 842.0f high.
        // We want a header of height 90 at the top of the page.
        cb.rectangle(0, doc.getPageSize().getHeight() - 100,
                doc.getPageSize().getWidth(), 100);
        cb.fill();
        cb.restoreState();

        // Bank name — large white text
        Font bankFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Font.NORMAL, Color.WHITE);
        Paragraph bankName = new Paragraph("Online Banking System", bankFont);
        bankName.setSpacingBefore(12f);
        doc.add(bankName);

        // Tagline
        Font tagFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.ITALIC, new Color(180, 190, 210));
        Paragraph tag = new Paragraph("Your Trusted Financial Partner", tagFont);
        tag.setSpacingAfter(4f);
        doc.add(tag);

        // Statement title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Font.NORMAL, BRAND_PURPLE);
        String rangeText = buildRangeText(from, to);
        Paragraph title = new Paragraph("ACCOUNT STATEMENT  —  " + rangeText, titleFont);
        title.setSpacingBefore(6f);
        title.setSpacingAfter(2f);
        doc.add(title);

        // Generated on
        Font genFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, new Color(100, 116, 139));
        doc.add(new Paragraph("Generated on: " + DT_FMT.format(Instant.now()), genFont));
    }

    private static String buildRangeText(LocalDate from, LocalDate to) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd MMM yyyy");
        if (from == null && to == null)
            return "All Transactions";
        if (from == null)
            return "Up to " + f.format(to);
        if (to == null)
            return "From " + f.format(from);
        return f.format(from) + " → " + f.format(to);
    }

    // ── Section 2 — Account info ───────────────────────────────────────────────
    private static void addAccountInfo(Document doc, Account account, Customer customer)
            throws DocumentException {

        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL, BRAND_DARK);
        Paragraph sectionTitle = new Paragraph("ACCOUNT DETAILS", sectionFont);
        sectionTitle.setSpacingAfter(6f);
        doc.add(sectionTitle);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 2f, 3f, 2f, 3f });
        table.setSpacingAfter(4f);

        Font lblFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Font.NORMAL, new Color(71, 85, 105));
        Font valFont = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Font.NORMAL, BRAND_DARK);

        String fullName = trim(customer.getFname()) + " " + trim(customer.getLname());
        addInfoRow(table, lblFont, valFont,
                "Account Holder", fullName,
                "Account Number", String.valueOf(account.getAccountNumber()));

        addInfoRow(table, lblFont, valFont,
                "Account Type", account.getAccountType() != null
                        ? account.getAccountType().name()
                        : "—",
                "Branch", trim(account.getBranch()));

        addInfoRow(table, lblFont, valFont,
                "IFSC Code", trim(account.getIfsc()),
                "Email", trim(customer.getEmail()));

        doc.add(table);
    }

    private static void addInfoRow(PdfPTable table,
            Font lblFont, Font valFont,
            String l1, String v1,
            String l2, String v2) {
        table.addCell(labelCell(l1, lblFont));
        table.addCell(valueCell(v1, valFont));
        table.addCell(labelCell(l2, lblFont));
        table.addCell(valueCell(v2, valFont));
    }

    private static PdfPCell labelCell(String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(new Color(241, 245, 249));
        c.setPadding(6);
        c.setBorderColor(BORDER_COLOR);
        return c;
    }

    private static PdfPCell valueCell(String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(Color.WHITE);
        c.setPadding(6);
        c.setBorderColor(BORDER_COLOR);
        return c;
    }

    // ── Section 3 — Transaction table ─────────────────────────────────────────
    private static void addTransactionTable(Document doc, List<Transaction> txs)
            throws DocumentException {

        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL, BRAND_DARK);
        Paragraph sectionTitle = new Paragraph("TRANSACTION HISTORY", sectionFont);
        sectionTitle.setSpacingAfter(6f);
        doc.add(sectionTitle);

        if (txs.isEmpty()) {
            Font emptyFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Font.NORMAL,
                    new Color(100, 116, 139));
            doc.add(new Paragraph("No transactions found for the selected period.", emptyFont));
            return;
        }

        // columns: # | Date & Time | Type | Amount | Balance | Reference
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 0.7f, 2.4f, 1.4f, 1.5f, 1.7f, 2.3f });
        table.setSpacingAfter(4f);
        table.setHeaderRows(1);

        // Header row
        Font hFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, Color.WHITE);
        String[] headers = { "#", "Date & Time", "Type", "Amount (₹)", "Balance (₹)", "Reference" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, hFont));
            cell.setBackgroundColor(TABLE_HEADER);
            cell.setPadding(7);
            cell.setBorderColor(TABLE_HEADER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Data rows
        Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, BRAND_DARK);
        Font typeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Font.NORMAL, BRAND_DARK);

        int rowNum = 1;
        for (Transaction t : txs) {
            boolean alt = (rowNum % 2 == 0);
            Color rowBg = alt ? ROW_ALT : Color.WHITE;

            // #
            table.addCell(dataCell(String.valueOf(rowNum), rowFont, rowBg,
                    Element.ALIGN_CENTER));

            // Date
            table.addCell(dataCell(DT_FMT.format(t.getTimestamp()), rowFont, rowBg,
                    Element.ALIGN_LEFT));

            // Type badge
            table.addCell(typeBadgeCell(t.getTransactionType(), typeFont, rowBg));

            // Amount (coloured)
            boolean isCredit = t.getTransactionType() == TransactionType.DEPOSIT
                    || (t.getTransactionType() == TransactionType.TRANSFER
                            && t.getReceiverAccountId() != null
                            && t.getReceiverAccountId().equals(
                                    t.getAccount() != null ? t.getAccount().getId() : null));
            Font amtFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Font.NORMAL,
                    isCredit ? CREDIT_GREEN : DEBIT_RED);
            String amtText = (isCredit ? "+" : "−") + " "
                    + (t.getAmount() != null ? t.getAmount().toPlainString() : "—");
            table.addCell(dataCell(amtText, amtFont, rowBg, Element.ALIGN_RIGHT));

            // Balance
            String bal = t.getRemainingBalance() != null
                    ? t.getRemainingBalance().toPlainString()
                    : "—";
            table.addCell(dataCell(bal, rowFont, rowBg, Element.ALIGN_RIGHT));

            // Reference
            String remark = t.getRemark() != null && !t.getRemark().isBlank()
                    ? t.getRemark()
                    : "—";
            table.addCell(dataCell(remark, rowFont, rowBg, Element.ALIGN_LEFT));

            rowNum++;
        }

        doc.add(table);
    }

    private static PdfPCell dataCell(String text, Font f, Color bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(bg);
        c.setPaddingTop(5);
        c.setPaddingBottom(5);
        c.setPaddingLeft(6);
        c.setPaddingRight(6);
        c.setBorderColor(BORDER_COLOR);
        c.setHorizontalAlignment(align);
        return c;
    }

    private static PdfPCell typeBadgeCell(TransactionType type, Font f, Color rowBg) {
        Color badgeColor;
        String label;
        switch (type) {
            case DEPOSIT -> {
                badgeColor = new Color(187, 247, 208);
                label = "DEPOSIT";
                f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Font.NORMAL, new Color(22, 101, 52));
            }
            case WITHDRAW -> {
                badgeColor = new Color(254, 202, 202);
                label = "WITHDRAW";
                f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Font.NORMAL, new Color(153, 27, 27));
            }
            case TRANSFER -> {
                badgeColor = new Color(219, 234, 254);
                label = "TRANSFER";
                f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Font.NORMAL, new Color(30, 64, 175));
            }
            default -> {
                badgeColor = new Color(241, 245, 249);
                label = type.name();
            }
        }
        PdfPCell c = new PdfPCell(new Phrase(label, f));
        c.setBackgroundColor(badgeColor);
        c.setPadding(5);
        c.setBorderColor(BORDER_COLOR);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }

    // ── Section 4 — Summary ────────────────────────────────────────────────────
    private static void addSummary(Document doc, List<Transaction> txs, BigDecimal closingBalance)
            throws DocumentException {

        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL, BRAND_DARK);
        Paragraph sectionTitle = new Paragraph("STATEMENT SUMMARY", sectionFont);
        sectionTitle.setSpacingAfter(6f);
        doc.add(sectionTitle);

        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalDebit = BigDecimal.ZERO;

        for (Transaction t : txs) {
            if (t.getTransactionType() == TransactionType.DEPOSIT) {
                totalCredit = totalCredit.add(t.getAmount());
            } else {
                totalDebit = totalDebit.add(t.getAmount());
            }
        }

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(55);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidths(new float[] { 2f, 2f });
        table.setSpacingAfter(6f);

        Font lblFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, new Color(71, 85, 105));
        Font valFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, BRAND_DARK);
        Font greenFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, CREDIT_GREEN);
        Font redFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, DEBIT_RED);
        Font navyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, Font.NORMAL, BRAND_DARK);

        addSummaryRow(table, lblFont, greenFont, "Total Credits", "₹ " + totalCredit.toPlainString());
        addSummaryRow(table, lblFont, redFont, "Total Debits", "₹ " + totalDebit.toPlainString());
        addSummaryRow(table, lblFont, valFont, "Total Transactions", String.valueOf(txs.size()));
        addSummaryRow(table, lblFont, navyFont, "Closing Balance",
                "₹ " + (closingBalance != null ? closingBalance.toPlainString() : "—"));

        doc.add(table);

        // Disclaimer
        Font discFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7.5f, Font.NORMAL,
                new Color(148, 163, 184));
        Paragraph disc = new Paragraph(
                "This is a computer-generated statement and does not require a signature. "
                        + "For queries, contact beautifulcake3002@gmail.com",
                discFont);
        disc.setSpacingBefore(6f);
        doc.add(disc);
    }

    private static void addSummaryRow(PdfPTable table, Font lblFont, Font valFont,
            String label, String value) {
        PdfPCell lc = new PdfPCell(new Phrase(label, lblFont));
        lc.setBackgroundColor(new Color(241, 245, 249));
        lc.setPadding(7);
        lc.setBorderColor(BORDER_COLOR);

        PdfPCell vc = new PdfPCell(new Phrase(value, valFont));
        vc.setBackgroundColor(Color.WHITE);
        vc.setPadding(7);
        vc.setBorderColor(BORDER_COLOR);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(lc);
        table.addCell(vc);
    }

    // ── Page footer event ──────────────────────────────────────────────────────
    static class FooterEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font footFont = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Font.NORMAL,
                    new Color(148, 163, 184));
            Phrase footer = new Phrase(
                    "Online Banking System  |  Confidential  |  Page " + writer.getPageNumber(),
                    footFont);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
                    (document.left() + document.right()) / 2,
                    document.bottom() - 18, 0);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private static String trim(String s) {
        return (s != null) ? s.trim() : "—";
    }
}
