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


// Declaring a WebServlet called InsertMovieServlet, which maps to url "/api/insert-movie"
@WebServlet(name = "InsertMovieServlet", urlPatterns = "/_dashboard/api/insert-movie")
public class InsertMovieServlet extends HttpServlet {
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
        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            String insertProcedure = "{CALL add_movie(?, ?, ?, ?, ?, ?)}";
            PreparedStatement statement = conn.prepareCall(insertProcedure);

            String title = request.getParameter("title").strip();
            String year = request.getParameter("year").strip();
            String director = request.getParameter("director").strip();
            String starName = request.getParameter("movieStar").strip();
            String genre = request.getParameter("genre").strip();

            PreparedStatement movieIdStatement = conn.prepareStatement("SELECT CONCAT('tt', CAST((CAST(SUBSTRING(max(id),3) AS UNSIGNED)+1) AS char(18))) AS id FROM movies");
            ResultSet rs = movieIdStatement.executeQuery();
            rs.next();
            String movieId = rs.getString("id");
            rs.close();
            movieIdStatement.close();

            statement.setString(1, movieId);
            statement.setString(2, title);
            statement.setInt(3, Integer.valueOf(year));
            statement.setString(4, director);
            statement.setString(5, starName);
            statement.setString(6, genre);

            statement.executeQuery();

            statement.close();

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
    }
}
