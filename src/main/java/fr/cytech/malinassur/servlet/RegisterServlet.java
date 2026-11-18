package fr.cytech.malinassur.servlet;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.service.AuthService;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        try {
            authService.registerClient(firstName, lastName, email, password, confirmPassword);
            req.setAttribute("success", "Votre compte a bien été créé. Vous pouvez maintenant vous connecter.");
            req.getRequestDispatcher("/WEB-INF/views/common/login.jsp").forward(req, resp);
        } catch (BusinessException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("firstName", firstName);
            req.setAttribute("lastName", lastName);
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/common/register.jsp").forward(req, resp);
        }
    }
}