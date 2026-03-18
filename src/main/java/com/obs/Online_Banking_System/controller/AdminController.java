package com.obs.Online_Banking_System.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.obs.Online_Banking_System.dto.AccountCreateDto;
import com.obs.Online_Banking_System.dto.AccountDto;
import com.obs.Online_Banking_System.dto.AdminDto;
import com.obs.Online_Banking_System.dto.CustomerDto;
import com.obs.Online_Banking_System.dto.TransactionDto;
import com.obs.Online_Banking_System.dto.TransactionRequestDto;
import com.obs.Online_Banking_System.dto.TransactionResponseDto;
import com.obs.Online_Banking_System.enumDto.AdminRole;
import com.obs.Online_Banking_System.service.AccountService;
import com.obs.Online_Banking_System.service.AdminService;
import com.obs.Online_Banking_System.service.CustomerService;
import com.obs.Online_Banking_System.service.TransactionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private TransactionService transactionService;

    // ── Role helpers ─────────────────────────────────────────────────────────

    /** ALL roles can register customers */
    private boolean canRegisterCustomer(HttpSession session) {
        String r = (String) session.getAttribute("adminRole");
        return "MANAGER".equals(r) || "ADMINISTRATIVE".equals(r) || "DIRECTOR".equals(r);
    }

    /**
     * ADMINISTRATIVE or DIRECTOR can manage other admins (register/update/delete)
     */
    private boolean isAdministrative(HttpSession session) {
        String r = (String) session.getAttribute("adminRole");
        return "ADMINISTRATIVE".equals(r) || "DIRECTOR".equals(r);
    }

    private boolean isDirector(HttpSession session) {
        return "DIRECTOR".equals(session.getAttribute("adminRole"));
    }

    /**
     * Helper to check if the current admin's role is allowed to manage the target
     * role.
     * Directors can manage Administrative and Manager.
     * Administrative can manage Manager.
     */
    private boolean canManageRole(String myRole, AdminRole targetRole) {
        if (myRole == null || targetRole == null)
            return false;
        if ("DIRECTOR".equals(myRole)) {
            return targetRole == AdminRole.ADMINISTRATIVE || targetRole == AdminRole.MANAGER;
        } else if ("ADMINISTRATIVE".equals(myRole)) {
            return targetRole == AdminRole.MANAGER;
        }
        return false;
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("loggedInAdmin") != null;
    }

    /**
     * Returns the AdminRole values this session's admin is allowed to assign when
     * registering
     */
    private java.util.List<AdminRole> allowedRolesToCreate(HttpSession session) {
        String r = (String) session.getAttribute("adminRole");
        if ("DIRECTOR".equals(r)) {
            return java.util.List.of(AdminRole.MANAGER, AdminRole.ADMINISTRATIVE, AdminRole.DIRECTOR);
        } else if ("ADMINISTRATIVE".equals(r)) {
            return java.util.List.of(AdminRole.MANAGER);
        }
        return java.util.Collections.emptyList();
    }

    @GetMapping("/demo")
    public ResponseEntity<String> demo() {
        return ResponseEntity.ok("Hello Admin");
    }

    @GetMapping("/getAdminByEmail")
    public ResponseEntity<AdminDto> getAdminByEmail(@RequestHeader String email) {
        log.info("Received request to get admin by email: {}", email);
        return adminService.getAdminByEmail(email);
    }

    @GetMapping("/getAccountByAccountNumber")
    public ResponseEntity<AccountDto> getAccountByAccountNumber(@RequestHeader Long accountNumber) {
        AccountDto accountDto = accountService.getAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(accountDto);
    }

    @GetMapping("/registerCustomer")
    public ResponseEntity<CustomerDto> registerCustomer(@RequestBody CustomerDto customerDto, HttpSession session) {
        if (!canRegisterCustomer(session))
            return ResponseEntity.status(403).build();
        CustomerDto cust = customerService.registerCustomer(customerDto);
        return ResponseEntity.ok(cust);
    }

    @GetMapping("/getCustomerByEmail")
    public ResponseEntity<CustomerDto> getCustomerByEmail(@RequestHeader String email) {
        CustomerDto cust = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(cust);
    }

    @GetMapping("/getCustomerByAdharcard")
    public ResponseEntity<CustomerDto> getCustomerByAdharcard(@RequestHeader Long adharcardNumber) {
        CustomerDto cust = customerService.getCustomerByAdharcard(adharcardNumber);
        return ResponseEntity.ok(cust);
    }

    @GetMapping("/updateCustomerByEmail")
    public ResponseEntity<CustomerDto> updateCustomerByEmail(@RequestHeader String email,
            @RequestBody CustomerDto customerDto) {
        return ResponseEntity.ok(customerService.updateCustomerByEmail(email, customerDto));
    }

    @PostMapping("/createAccount")
    public ResponseEntity<AccountDto> createAccount(@RequestBody AccountCreateDto account, HttpSession session) {
        if (!canRegisterCustomer(session))
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(accountService.createAccount(account));
    }

    // ---- Admin registration page (role-restricted, session-gated) ----

    /**
     * GET /admin/register-admin
     * Director → can register MANAGER, ADMINISTRATIVE, DIRECTOR
     * Administrative → can register MANAGER only
     * Manager → no access (redirect to dashboard)
     */
    @GetMapping("/register-admin")
    public String showRegisterAdminPage(HttpSession session, Model model, RedirectAttributes ra) {
        if (!isLoggedIn(session))
            return "redirect:/login-admin";
        java.util.List<AdminRole> allowed = allowedRolesToCreate(session);
        if (allowed.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "You do not have permission to register admins.");
            return "redirect:/admin/dashboard-admin";
        }
        model.addAttribute("admin", new AdminDto());
        model.addAttribute("allowedRoles", allowed);
        return "register-admin";
    }

    /**
     * POST /admin/register-admin — validates submitted role is within caller's
     * allowed set
     */
    @PostMapping("/register-admin")
    public String handleRegisterAdmin(
            @ModelAttribute("admin") AdminDto adminDto,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {
        if (!isLoggedIn(session))
            return "redirect:/login-admin";
        java.util.List<AdminRole> allowed = allowedRolesToCreate(session);
        if (allowed.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "You do not have permission to register admins.");
            return "redirect:/admin/dashboard-admin";
        }
        // Privilege-escalation guard: submitted role must be in allowed set
        if (adminDto.getAdminRole() == null || !allowed.contains(adminDto.getAdminRole())) {
            model.addAttribute("error", "Invalid role selection.");
            model.addAttribute("admin", adminDto);
            model.addAttribute("allowedRoles", allowed);
            return "register-admin";
        }
        try {
            adminService.register(adminDto);
            model.addAttribute("success", "Admin account created successfully!");
            model.addAttribute("admin", new AdminDto());
        } catch (Exception e) {
            model.addAttribute("error",
                    e.getMessage() != null ? e.getMessage() : "Registration failed. Please try again.");
            model.addAttribute("admin", adminDto);
        }
        model.addAttribute("allowedRoles", allowed);
        return "register-admin";
    }

    @GetMapping("/login")
    public String loginAdmin(Model model) {
        model.addAttribute("admin", new AdminDto());
        return new String();
    }

    @PostMapping("/login-admin")
    public String loginAdmin(Model model,
            @RequestParam(name = "email") String email,
            @RequestParam(name = "password") String password,
            HttpServletRequest request) throws IOException {

        Map<String, Object> response = new HashMap<>();

        response = adminService.athenticateAdminMap(email, password);

        if (response.containsKey("error")) {
            String msg = new String(response.get("error").toString());
            model.addAttribute("error", msg);
            return "login-admin";
        }

        AdminDto adminDto = (AdminDto) response.get("admin");

        HttpSession session = request.getSession(true);
        session.setAttribute("loggedInAdmin", adminDto);
        session.setAttribute("adminId", adminDto.getAdminId());
        session.setAttribute("email", adminDto.getEmail());
        session.setAttribute("adharcard", adminDto.getAdharcard());
        session.setAttribute("adminRole", adminDto.getAdminRole() != null ? adminDto.getAdminRole().name() : "MANAGER");

        // wait for 5 seconds before redirecting to dashboard
        model.addAttribute("success", "Login successful");
        // model.addAttribute("redirectDelayMs", 5);
        model.addAttribute("redirectUrl", "/admin/dashboard-admin");

        return "login-admin";
    }

    @GetMapping("/dashboard-admin")
    public String dashboard(HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if admin is logged in by looking for the session attribute
        AdminDto adminDto = (AdminDto) session.getAttribute("loggedInAdmin");

        if (adminDto != null) {
            // admin is authenticated, allow access to dashboard
            return "dashboard-admin";
        } else {
            // admin is not authenticated, redirect to login
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to access the dashboard");
            return "redirect:/login-admin";
        }
    }

    // GET /admin/api/customers
    @GetMapping("/api/customers")
    @ResponseBody
    public ResponseEntity<List<CustomerDto>> getAllCustomers(HttpSession session,
            RedirectAttributes redirectAttributes) {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    // GET /admin/api/accounts
    @GetMapping("/api/accounts")
    @ResponseBody
    public ResponseEntity<List<AccountDto>> getAllAccounts(HttpSession session) {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    /** Admin: total transaction count */
    @GetMapping("/api/transactions/count")
    @ResponseBody
    public ResponseEntity<Long> getTotalTransactionCount(HttpSession session) {
        return ResponseEntity.ok(transactionService.getAllTransactionsCount());
    }

    /** Admin: all transactions API for AJAX datatables */
    @GetMapping("/api/transactions")
    @ResponseBody
    public ResponseEntity<List<TransactionDto>> getAllTransactionsAPI(HttpSession session) {
        if (!isLoggedIn(session))
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(transactionService.findAllTransactions());
    }

    /** Admin: get all admins API for Manage Admins page */
    @GetMapping("/api/admins")
    @ResponseBody
    public ResponseEntity<List<AdminDto>> getAllAdminsAPI(HttpSession session) {
        if (!isAdministrative(session))
            return ResponseEntity.status(403).build();

        String myRole = (String) session.getAttribute("adminRole");
        Long loggedInAdminId = (Long) session.getAttribute("adminId");

        List<AdminDto> allAdmins = adminService.getAllAdmins();
        List<AdminDto> managedAdmins = allAdmins.stream()
                .filter(a -> !a.getAdminId().equals(loggedInAdminId) && canManageRole(myRole, a.getAdminRole()))
                .toList();
        return ResponseEntity.ok(managedAdmins);
    }

    /** Admin: get all customer transactions for account profile */
    @GetMapping("/api/customer-transactions/{email}")
    @ResponseBody
    public ResponseEntity<List<TransactionResponseDto>> getCustomerTransactionsAPI(@PathVariable String email,
            HttpSession session) {
        if (!isLoggedIn(session))
            return ResponseEntity.status(401).build();
        try {
            return ResponseEntity.ok(transactionService.getAllTransactions(email));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /** Admin: All accounts page (optional ?type=SAVINGS or ?type=CURRENT) */
    @GetMapping("/all-accounts")
    public String allAccounts(@RequestParam(required = false) String type,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {
        if (session.getAttribute("loggedInAdmin") == null) {
            ra.addFlashAttribute("errorMessage", "Please login first");
            return "redirect:/login-admin";
        }
        List<AccountDto> accounts = accountService.getAllAccounts();
        if (type != null && !type.isBlank()) {
            String upper = type.toUpperCase();
            accounts = accounts.stream()
                    .filter(a -> a.getAccountType() != null && a.getAccountType().name().equalsIgnoreCase(upper))
                    .toList();
            model.addAttribute("filterType", upper);
        }
        model.addAttribute("accounts", accounts);
        return "admin-all-accounts";
    }

    @GetMapping("/api/customers/count")
    public ResponseEntity<Long> getTotalCustomerCount(HttpSession session) {
        return ResponseEntity.ok(customerService.getAllCustomerCount());
    }

    /** Admin: All transactions page */
    @GetMapping("/all-transactions")
    public String allTransactions(HttpSession session,
            Model model,
            RedirectAttributes ra) {
        if (session.getAttribute("loggedInAdmin") == null) {
            ra.addFlashAttribute("errorMessage", "Please login first");
            return "redirect:/login-admin";
        }
        // Data loaded via AJAX
        return "admin-all-transactions";
    }

    /** Admin: Deposit &amp; Withdraw page */
    @GetMapping("/transact")
    public String transactPage(HttpSession session, RedirectAttributes ra) {
        if (session.getAttribute("loggedInAdmin") == null) {
            ra.addFlashAttribute("errorMessage", "Please login first");
            return "redirect:/login-admin";
        }
        return "admin-transact";
    }

    /** Admin: API — only DEPOSIT and WITHDRAW transactions */
    @GetMapping("/api/transactions/deposit-withdraw")
    @ResponseBody
    public ResponseEntity<List<TransactionDto>> getDepositWithdrawTransactionsAPI(HttpSession session) {
        if (!isLoggedIn(session))
            return ResponseEntity.status(401).build();
        List<TransactionDto> all = transactionService.findAllTransactions();
        List<TransactionDto> filtered = all.stream()
                .filter(t -> t.getTransactionType() != null &&
                        (t.getTransactionType().name().equals("DEPOSIT") ||
                         t.getTransactionType().name().equals("WITHDRAW")))
                .toList();
        return ResponseEntity.ok(filtered);
    }

    /** Admin: All customers page */
    @GetMapping("/all-customers")
    public String allCustomers(HttpSession session,
            RedirectAttributes ra) {
        if (session.getAttribute("loggedInAdmin") == null) {
            ra.addFlashAttribute("errorMessage", "Please login first");
            return "redirect:/login-admin";
        }
        return "admin-all-customers";
    }

    /** Admin: view a specific customer's profile */
    @GetMapping("/customer-profile/{id}")
    public String customerProfile(@PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {
        if (session.getAttribute("loggedInAdmin") == null) {
            ra.addFlashAttribute("errorMessage", "Please login first");
            return "redirect:/login-admin";
        }
        try {
            CustomerDto customer = customerService.getCustomerById(id);
            model.addAttribute("customer", customer);
            return "admin-customer-profile";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Customer not found");
            return "redirect:/admin/dashboard-admin";
        }
    }

    /** Admin: view a specific customer's account details */
    @GetMapping("/customer-account/{id}")
    public String customerAccount(@PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes ra) {
        if (session.getAttribute("loggedInAdmin") == null) {
            ra.addFlashAttribute("errorMessage", "Please login first");
            return "redirect:/login-admin";
        }
        try {
            CustomerDto customer = customerService.getCustomerById(id);
            AccountDto account = accountService.getAccountByCustomerEmail(customer.getEmail());
            List<TransactionResponseDto> transactions = transactionService.getAllTransactions(customer.getEmail());
            model.addAttribute("customer", customer);
            model.addAttribute("account", account);
            model.addAttribute("transactions", transactions);
            return "admin-customer-account";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Account not found for this customer");
            ra.addFlashAttribute("errorMessage", "Account not found for this customer");
            return "redirect:/admin/dashboard-admin";
        }
    }

    // ── Admin Management (ADMINISTRATIVE-only) ─────────────────────────────────

    /** View all MANAGER accounts */
    @GetMapping("/manage-admins")
    public String manageAdmins(HttpSession session, Model model, RedirectAttributes ra) {
        if (!isLoggedIn(session))
            return "redirect:/login-admin";
        if (!isAdministrative(session)) {
            ra.addFlashAttribute("errorMessage", "Access denied. Administrative role required.");
            return "redirect:/admin/dashboard-admin";
        }

        // Fetch all admins and filter by who this admin can manage
        Long loggedInAdminId = (Long) session.getAttribute("adminId");
        String myRole = (String) session.getAttribute("adminRole");

        List<AdminDto> allAdmins = adminService.getAllAdmins();
        List<AdminDto> managedAdmins = allAdmins.stream()
                .filter(a -> !a.getAdminId().equals(loggedInAdminId) && canManageRole(myRole, a.getAdminRole()))
                .toList();

        model.addAttribute("managers", managedAdmins);
        return "admin-manage-admins";
    }

    /** Update an admin (name, email, phone, role, password) */
    @PostMapping("/update-admin/{id}")
    public String updateAdminById(@PathVariable Long id,
            @ModelAttribute AdminDto adminDto,
            HttpSession session,
            RedirectAttributes ra) {
        if (!isLoggedIn(session))
            return "redirect:/login-admin";
        if (!isAdministrative(session)) {
            ra.addFlashAttribute("errorMessage", "Access denied.");
            return "redirect:/admin/dashboard-admin";
        }

        try {
            AdminDto targetAdmin = adminService.getAdminById(id).getBody();
            if (targetAdmin != null
                    && !canManageRole((String) session.getAttribute("adminRole"), targetAdmin.getAdminRole())) {
                ra.addFlashAttribute("errorMessage", "Access denied. You cannot manage this role.");
                return "redirect:/admin/manage-admins";
            }

            adminService.fullUpdateAdmin(id, adminDto);
            ra.addFlashAttribute("successMessage", "Admin updated successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Update failed: " + e.getMessage());
        }
        return "redirect:/admin/manage-admins";
    }

    /** Delete an admin account */
    @PostMapping("/delete-admin/{id}")
    public String deleteAdminById(@PathVariable Long id,
            HttpSession session,
            RedirectAttributes ra) {
        if (!isLoggedIn(session))
            return "redirect:/login-admin";
        if (!isAdministrative(session)) {
            ra.addFlashAttribute("errorMessage", "Access denied.");
            return "redirect:/admin/dashboard-admin";
        }

        try {
            AdminDto targetAdmin = adminService.getAdminById(id).getBody();
            if (targetAdmin != null
                    && !canManageRole((String) session.getAttribute("adminRole"), targetAdmin.getAdminRole())) {
                ra.addFlashAttribute("errorMessage", "Access denied. You cannot delete this role.");
                return "redirect:/admin/manage-admins";
            }

            adminService.deleteAdmin(id);
            ra.addFlashAttribute("successMessage", "Admin deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Delete failed: " + e.getMessage());
        }
        return "redirect:/admin/manage-admins";
    }

    /** API: get one admin by ID (for edit modal form) */
    @GetMapping("/api/admin/{id}")
    @ResponseBody
    public ResponseEntity<AdminDto> getAdminById(@PathVariable Long id, HttpSession session) {
        if (!isAdministrative(session))
            return ResponseEntity.status(403).build();

        ResponseEntity<AdminDto> response = adminService.getAdminById(id);
        AdminDto targetAdmin = response.getBody();
        if (targetAdmin != null
                && !canManageRole((String) session.getAttribute("adminRole"), targetAdmin.getAdminRole())) {
            return ResponseEntity.status(403).build();
        }

        return response;
    }

    /** GET /admin/profile – show the profile of the currently logged-in admin */
    @GetMapping("/profile")
    public String adminProfile(HttpSession session, Model model, RedirectAttributes ra) {
        if (!isLoggedIn(session)) {
            ra.addFlashAttribute("errorMessage", "Please login to view your profile.");
            return "redirect:/login-admin";
        }
        AdminDto admin = (AdminDto) session.getAttribute("loggedInAdmin");
        model.addAttribute("admin", admin);
        return "admin-profile";
    }

    /** GET /admin/profile/{id} – show the profile of a specific admin */
    @GetMapping("/profile/{id}")
    public String viewAdminProfile(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
        if (!isLoggedIn(session)) {
            ra.addFlashAttribute("errorMessage", "Please login to view profiles.");
            return "redirect:/login-admin";
        }
        if (!isAdministrative(session)) {
            ra.addFlashAttribute("errorMessage", "Access denied. Administrative role required.");
            return "redirect:/admin/dashboard-admin";
        }
        try {
            AdminDto admin = adminService.getAdminById(id).getBody();
            if (admin != null && !canManageRole((String) session.getAttribute("adminRole"), admin.getAdminRole())) {
                ra.addFlashAttribute("errorMessage", "Access denied. You cannot view this profile.");
                return "redirect:/admin/manage-admins";
            }

            model.addAttribute("admin", admin);
            // Flag to conditionally hide the "Edit Profile" button if viewing someone else
            model.addAttribute("isOwnProfile", false);
            return "admin-profile";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Admin not found.");
            return "redirect:/admin/manage-admins";
        }
    }

    /**
     * POST /admin/update-profile – updates the currently logged-in admin's profile
     */
    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute AdminDto adminDto, HttpSession session, RedirectAttributes ra) {
        if (!isLoggedIn(session)) {
            ra.addFlashAttribute("errorMessage", "Please login to update your profile.");
            return "redirect:/login-admin";
        }

        try {
            Long adminId = (Long) session.getAttribute("adminId");
            // Perform the update using the existing method
            AdminDto updatedAdmin = adminService.fullUpdateAdmin(adminId, adminDto);

            // Update the session attributes with the new details
            session.setAttribute("loggedInAdmin", updatedAdmin);
            session.setAttribute("email", updatedAdmin.getEmail());

            ra.addFlashAttribute("successMessage", "Profile updated successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
        }

        return "redirect:/admin/profile";
    }

    /**
     * POST /admin/api/account/{id}/lock — locks an account
     */
    @PostMapping("/api/account/{id}/lock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> lockAccount(@PathVariable Long id, HttpSession session) {
        if (!isLoggedIn(session))
            return ResponseEntity.status(401).build();
        Map<String, Object> result = new HashMap<>();
        try {
            AccountDto acc = accountService.lockAccount(id);
            result.put("locked", acc.isLocked());
            result.put("message", "Account locked successfully.");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * POST /admin/api/account/{id}/unlock — unlocks an account
     */
    @PostMapping("/api/account/{id}/unlock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unlockAccount(@PathVariable Long id, HttpSession session) {
        if (!isLoggedIn(session))
            return ResponseEntity.status(401).build();
        Map<String, Object> result = new HashMap<>();
        try {
            AccountDto acc = accountService.unlockAccount(id);
            result.put("locked", acc.isLocked());
            result.put("message", "Account unlocked successfully.");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * POST /admin/api/account/{id}/deposit — Admin bank deposit
     */
    @PostMapping("/api/account/{id}/deposit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adminDeposit(@PathVariable Long id,
            @RequestBody Map<String, String> payload, HttpSession session) {
        if (!isLoggedIn(session))
            return ResponseEntity.status(401).build();
        Map<String, Object> result = new HashMap<>();
        try {
            AccountDto account = accountService.getAccountById(id);
            String email = account.getCustomer().getEmail();

            TransactionRequestDto tx = new TransactionRequestDto();
            tx.setAmount(new java.math.BigDecimal(payload.get("amount")));
            tx.setRemark("Bank deposit");
            tx.setTargetAccountNo(account.getAccountNumber());

            String msg = transactionService.deposit(tx, email);
            result.put("success", true);
            result.put("message", msg);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * POST /admin/api/account/{id}/withdraw — Admin bank withdraw
     */
    @PostMapping("/api/account/{id}/withdraw")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adminWithdraw(@PathVariable Long id,
            @RequestBody Map<String, String> payload, HttpSession session) {
        if (!isLoggedIn(session))
            return ResponseEntity.status(401).build();
        Map<String, Object> result = new HashMap<>();
        try {
            AccountDto account = accountService.getAccountById(id);
            String email = account.getCustomer().getEmail();

            TransactionRequestDto tx = new TransactionRequestDto();
            tx.setAmount(new java.math.BigDecimal(payload.get("amount")));
            tx.setRemark("bank withdraw");
            tx.setTargetAccountNo(account.getAccountNumber());

            String msg = transactionService.withdraw(tx, email);
            result.put("success", true);
            result.put("message", msg);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

}
