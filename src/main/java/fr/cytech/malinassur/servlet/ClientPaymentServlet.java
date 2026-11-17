package fr.cytech.malinassur.servlet;

import java.io.IOException;
import java.util.List;

import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.Payment;
import fr.cytech.malinassur.model.PaymentMethod;
import fr.cytech.malinassur.service.PaymentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/client/payments")
public class ClientPaymentServlet extends HttpServlet {

    private final PaymentService paymentService = new PaymentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long clientId = resolveClientId(req);
        List<Payment> payments = paymentService.findByClient(clientId);
        req.setAttribute("payments", payments);
        req.setAttribute("paymentMethods", PaymentMethod.values());
        req.getRequestDispatcher("/WEB-INF/views/client/payments.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long clientId = resolveClientId(req);
        try {
            Long paymentId = Long.parseLong(req.getParameter("paymentId"));
            PaymentMethod method = PaymentMethod.valueOf(req.getParameter("paymentMethod"));

            Payment paid = paymentService.payInstallment(paymentId, clientId, method);
            req.getSession().setAttribute("success", "Paiement " + paid.getTransactionReference() + " validé.");
        } catch (BusinessException | IllegalArgumentException e) {
            req.getSession().setAttribute("error", e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/client/payments");
    }

    private Long resolveClientId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object userObj = session.getAttribute("user");
            if (userObj instanceof AppUser u) return u.getId();
            Object idObj = session.getAttribute("userId");
            if (idObj instanceof Long id) return id;
        }
        return 4L;
    }
}