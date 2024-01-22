import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/shopping-cart"
@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

     /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String item = request.getParameter("item");

        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        TreeMap<String, Integer> previousItems = (TreeMap<String, Integer>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new TreeMap<String, Integer>();
            previousItems.put(item, 1);
            session.setAttribute("previousItems", previousItems);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousItems) {
                if (previousItems.containsKey(item)) {
                    previousItems.replace(item, previousItems.get(item)+1);
                }
                else {
                    previousItems.put(item, 1);
                }
            }
        }

        JsonObject responseJsonObject = new JsonObject();

        for (Map.Entry<String, Integer> entry : previousItems.entrySet()) {
            responseJsonObject.addProperty(entry.getKey(), entry.getValue());
        }

        response.getWriter().write(responseJsonObject.toString());
    }
}