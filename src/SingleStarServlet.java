import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
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

            String query = "SELECT movieId,title,year FROM stars_in_movies,stars,movies WHERE movieId=movies.id AND starId=stars.id AND stars.id=? ORDER BY year DESC, title ASC";
            String starId = request.getParameter("starId");

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, starId);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonObject jsonObject = new JsonObject();

            // Iterate through each row of rs
            String movies = "";
            while (rs.next()) {
                String movieId = rs.getString("movieId");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");

                movies += "<th><a href=single-movie.html?movieId=" + movieId + ">" + movieTitle + " (" + movieYear + ")</a></th>";

            }
            jsonObject.addProperty("movies", movies);
            rs.close();
            statement.close();

            query = "SELECT name, birthYear FROM stars WHERE id=?";
            statement = conn.prepareStatement(query);
            statement.setString(1, starId);
            rs = statement.executeQuery();
            while (rs.next()) {
                String starName = rs.getString("name");
                String birthYear = rs.getString("birthYear");

                jsonObject.addProperty("starName", starName);
                jsonObject.addProperty("birthYear", birthYear);
            }
 
            // Write JSON string to output
            out.write(jsonObject.toString());
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

}