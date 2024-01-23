import com.google.gson.JsonArray;
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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource
            HttpSession session = request.getSession();
            TreeMap<String, Integer> previousItems = (TreeMap<String, Integer>) session.getAttribute("previousItems");

            String query = "SELECT title, id FROM movies WHERE id=?";
            PreparedStatement statement = conn.prepareStatement(query);

            JsonArray jsonArray = new JsonArray();
            for (Map.Entry<String,Integer> entry : previousItems.entrySet()) {
                statement.setString(1, entry.getKey());
                ResultSet rs = statement.executeQuery();

                // Iterate through each row of rs
                while (rs.next()) {
                    String movieTitle = rs.getString("title");
                    String movieId = rs.getString("id");

                    // Create a JsonObject based on the data we retrieve from rs

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movieTitle", movieTitle);
                    jsonObject.addProperty("movieId", movieId);
                    jsonObject.addProperty("quantity", entry.getValue());

                    jsonArray.add(jsonObject);
                }
                rs.close();
            }
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

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
        } 
        else {
            String action = request.getParameter("action");
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            if (action.equals("subtract")) {
                synchronized (previousItems) {
                    if (previousItems.get(item) == 1) {
                        previousItems.remove(item);
                    }
                    else {
                        previousItems.replace(item, previousItems.get(item)-1); 
                    }
                }
            }
            else if (action.equals("remove")) {
                previousItems.remove(item);
            }
            else if (action.equals("add")) {
                synchronized (previousItems) {
                    if (previousItems.containsKey(item)) {
                        previousItems.replace(item, previousItems.get(item)+1);
                    }
                    else {
                        previousItems.put(item, 1);
                    }
                }
            }
        }
        session.setAttribute("previousItems", previousItems);
        JsonObject responseJsonObject = new JsonObject();

        for (Map.Entry<String, Integer> entry : previousItems.entrySet()) {
            responseJsonObject.addProperty(entry.getKey(), entry.getValue());
        }

        response.getWriter().write(responseJsonObject.toString());
    }
}