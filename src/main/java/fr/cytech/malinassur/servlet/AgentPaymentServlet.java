package fr.cytech.malinassur.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import fr.cytech.malinassur.dao.ContractDao;
import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.PaymentMethod;
import fr.cytech.malinassur.service.PaymentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/agent/payments")
public class AgentPaymentServlet extends HttpServlet {

    private final PaymentService paymentService = new PaymentService();
    private final ContractDao contractDao = new ContractDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("payments", paymentService.findAll());
        req.setAttribute("contracts", contractDao.findAll());
        req.setAttribute("paymentMethods", PaymentMethod.values());
        req.getRequestDispatcher("/WEB-INF/views/agent/payments.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        try {
            switch (action) {
                case "createSchedule" -> {
                    Long contractId = Long.parseLong(req.getParameter("contractId"));
                    BigDecimal amount = new BigDecimal(req.getParameter("amount").replace(",", "."));
                    LocalDate dueDate = LocalDate.parse(req.getParameter("dueDate"));
                    paymentService.createSchedule(contractId, amount, dueDate);
                    req.getSession().setAttribute("success", "Échéance de paiement générée.");
                }
                case "recordDirect" -> {
                    Long contractId = Long.parseLong(req.getParameter("contractId"));
                    BigDecimal amount = new BigDecimal(req.getParameter("amount").replace(",", "."));
                    PaymentMethod method = PaymentMethod.valueOf(req.getParameter("paymentMethod"));
                    paymentService.recordDirectPayment(contractId, amount, method);
                    req.getSession().setAttribute("success", "Règlement direct enregistré.");
                }
                case "validate" -> {
                    Long paymentId = Long.parseLong(req.getParameter("paymentId"));
                    PaymentMethod method = PaymentMethod.valueOf(req.getParameter("paymentMethod"));
                    paymentService.payInstallment(paymentId, null, method);
                    req.getSession().setAttribute("success", "Paiement validé manuellement.");
                }
                case "fail" -> {
                    Long paymentId = Long.parseLong(req.getParameter("paymentId"));
                    paymentService.markAsFailed(paymentId);
                    req.getSession().setAttribute("success", "Paiement marqué en échec.");
                }
                default -> throw new BusinessException("Action de paiement invalide.");
            }
        } catch (Exception e) {
            req.getSession().setAttribute("error", e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/agent/payments");
    }
}