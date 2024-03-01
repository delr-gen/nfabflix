import com.google.gson.JsonObject;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (Connection conn = dataSource.getConnection()){
            String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

            System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

            PrintWriter out = response.getWriter();
            // Verify reCAPTCHA
            JsonObject responseJsonObject = new JsonObject();

            if (gRecaptchaResponse!=null) {
                try {
                    RecaptchaVerifyUtils.verify(gRecaptchaResponse);
                } catch (Exception e) {
                    // Login fail
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Login failed");
                    // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                    responseJsonObject.addProperty("message", "Failed captcha");

                    out.write(responseJsonObject.toString());
                    response.setStatus(200);
                    out.close();
                }
            }

            String username = request.getParameter("username");
            String password = request.getParameter("password");

            /* This example only allows username/password to be test/test
            /  in the real project, you should talk to the database to verify username/password
            */

            String loginQuery = "SELECT id, password FROM customers WHERE email=?";
            PreparedStatement ps = conn.prepareStatement(loginQuery);
            ps.setString(1, username);
            //ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            rs.next();

            try  {
                // Login success:
                String encryptedPassword = rs.getString("password");
                boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                
                if (success) {
                    // set this user into the session
                    request.getSession().setAttribute("user", new User(username, rs.getString("id")));

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                }
                else {
                    // Login fail
                    responseJsonObject.addProperty("status", "fail");
                    // Log to localhost log
                    request.getServletContext().log("Login failed");
                    // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                    responseJsonObject.addProperty("message", "Wrong username or password");                    
                }

            } catch (SQLException e) {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Login failed");
                // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                responseJsonObject.addProperty("message", "Wrong username or password");

            }
            response.getWriter().write(responseJsonObject.toString());

            rs.close();
            ps.close();
        } catch (Exception e) {
			System.out.println(e);
			response.sendError(500, e.getMessage());
		}
    }
}