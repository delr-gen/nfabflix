import com.google.gson.JsonArray;
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


// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/movies"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
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

            String query = "SELECT title,year,director,rating FROM movies, ratings WHERE movies.id=ratings.movieId AND movies.id=?";
            String movieId = request.getParameter("movieId");

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, movieId);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            String starQuery = "SELECT starId,name FROM stars, stars_in_movies WHERE starId IN (SELECT starId FROM stars_in_movies WHERE movieId=?) AND starId=id GROUP BY starId ORDER BY count(starId) DESC, name ASC";
            PreparedStatement starStatement = conn.prepareStatement(starQuery);

            String genreQuery = "SELECT name, genreId FROM genres, genres_in_movies AS gm WHERE id=gm.genreId AND movieId=? ORDER BY name";
            PreparedStatement genreStatement = conn.prepareStatement(genreQuery);
            // Iterate through each row of rs
            while (rs.next()) {
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieRating = rs.getString("rating");

                starStatement.setString(1, movieId);
                ResultSet starResultSet = starStatement.executeQuery();
                String starHtml = "";
                while (starResultSet.next()) {
                    String starName = starResultSet.getString("name");
                    String starId = starResultSet.getString("starId");

                    starHtml += "<a href=single-star.html?starId=" + starId + ">" + starName + "</a><br>";
                }
                starStatement.close();

                genreStatement.setString(1, movieId);
                ResultSet genreResultSet = genreStatement.executeQuery();
                String genreHtml = "";
                while (genreResultSet.next()) {
                    String genreName = genreResultSet.getString("name");
                    String genreId = genreResultSet.getString("genreId");

                    genreHtml += "<a href=movies.html?genreId=" + genreId + ">" + genreName + "</a><br>";
                }
                genreStatement.close();

                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_rating", movieRating);
                jsonObject.addProperty("movie_stars", starHtml.toString());
                jsonObject.addProperty("movie_genres", genreHtml.toString());

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();
            starStatement.close();
            genreStatement.close();

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

}
