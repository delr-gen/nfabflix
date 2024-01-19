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

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/movies"
@WebServlet(name = "TableServlet", urlPatterns = "/api/movies")
public class TableServlet extends HttpServlet {
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

            int page = 0;
            HttpSession session = request.getSession();
            String pageTrue = (String) request.getParameter("page");

            String movieTitle;
            String movieYear;
            String movieDirector;
            String movieStar;
            String genreId;
            String sortBy;
            int show;

            if (request.getParameterMap().isEmpty()) {
                movieTitle = (String) session.getAttribute("movieTitle");
                movieYear = (String) session.getAttribute("movieYear");
                movieDirector = (String) session.getAttribute("movieDirector");
                movieStar = (String) session.getAttribute("movieStar");
                genreId = (String) session.getAttribute("genreId");
                sortBy = (String) session.getAttribute("sortBy");
                show = (int) session.getAttribute("show");
                page = (int) session.getAttribute("page");
            }
            else if (pageTrue != null && !pageTrue.equals("")) {
                page = Integer.valueOf(pageTrue);

                movieTitle = (String) session.getAttribute("movieTitle");
                movieYear = (String) session.getAttribute("movieYear");
                movieDirector = (String) session.getAttribute("movieDirector");
                movieStar = (String) session.getAttribute("movieStar");
                genreId = (String) session.getAttribute("genreId");
                sortBy = (String) session.getAttribute("sortBy");
                show = Integer.parseInt(session.getAttribute("show").toString());
                session.setAttribute("page", page);
            }
            else {
                // get parameters
                movieTitle = request.getParameter("title");
                movieYear = request.getParameter("year");
                movieDirector = request.getParameter("director");
                movieStar = request.getParameter("star");
                genreId = request.getParameter("genreId");
                sortBy = request.getParameter("sort");
                show = Integer.valueOf(request.getParameter("show"));

                session.setAttribute("sortBy", sortBy);
                session.setAttribute("show", show);
                session.setAttribute("page", page);
            }

            String query = "";
            PreparedStatement statement = null;


            if (genreId != null && !genreId.equals("")) {
                genreId = genreId.strip();
                session.setAttribute("genreId", genreId);
                movieStar = "";
                query = "SELECT gm.movieId,title,year,director,rating from movies, ratings, genres, genres_in_movies AS gm WHERE movies.id=ratings.movieId AND gm.movieId=movies.id AND gm.genreId=genres.id AND genres.id=? ORDER BY " + sortBy + " LIMIT " + Integer.toString(show+1) + " OFFSET " + Integer.toString(page * show);;

                // Declare our statement
                statement = conn.prepareStatement(query);
                statement.setInt(1, Integer.valueOf(genreId));
            }
            else {
                movieTitle = movieTitle.strip();
                movieYear = movieYear.strip();
                movieDirector = movieDirector.strip();
                movieStar = movieStar.strip();

                session.setAttribute("movieTitle", movieTitle);
                session.setAttribute("movieYear", movieYear);
                session.setAttribute("movieDirector", movieDirector);
                session.setAttribute("movieStar", movieStar);

                String search = " title LIKE ? AND director LIKE ? ";
                if (movieYear != "") {
                    search += "AND year=? ";
                }
                query = "SELECT movieId,title,year,director,rating from movies,ratings WHERE movies.id=ratings.movieId AND" + search + "ORDER BY " + sortBy + " LIMIT " + Integer.toString(show+1) + " OFFSET " + Integer.toString(page * show);
                if (movieStar!=null && !movieStar.equals("")) {
                    search += "AND stars.name LIKE ? ";
                    query = "SELECT DISTINCT sm.movieId,title,year,director,rating from movies, ratings, stars, stars_in_movies AS sm WHERE sm.movieId=movies.id AND movies.id=ratings.movieId AND stars.id=sm.starId AND" + search + "ORDER BY " + sortBy + " LIMIT " + Integer.toString(show+1) + " OFFSET " + Integer.toString(page * show);
                }

                // Declare our statement
                statement = conn.prepareStatement(query);

                // Set parameters
                statement.setString(1, "%" + movieTitle + "%");
                statement.setString(2, "%" + movieDirector + "%");
                int index = 3;
                if (movieYear!=null && !movieYear.equals("")) {
                    statement.setInt(index, Integer.valueOf(movieYear));
                    index += 1;
                }
                if (movieStar!=null && !movieStar.equals("")) {
                    statement.setString(index, "%" + movieStar + "%");
                }
            }

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            String starQuery = "SELECT starId,name FROM stars, stars_in_movies WHERE starId IN (SELECT starId FROM stars_in_movies WHERE movieId=? AND name LIKE ?) AND starId=id GROUP BY starId ORDER BY count(starId) DESC, name ASC LIMIT 3";
            PreparedStatement starStatement = conn.prepareStatement(starQuery);
            starStatement.setString(2, "%" + movieStar + "%");

            String genreQuery = "SELECT name, genreId FROM genres, genres_in_movies AS gm WHERE id=gm.genreId AND movieId=? ORDER BY name LIMIT 3";
            PreparedStatement genreStatement = conn.prepareStatement(genreQuery);
            // Iterate through each row of rs
            while (rs.next()) {
                String movieId = rs.getString("movieId");
                movieTitle = rs.getString("title");
                movieYear = rs.getString("year");
                movieDirector = rs.getString("director");
                String movieRating = rs.getString("rating");

                starStatement.setString(1, movieId);
                ResultSet starResultSet = starStatement.executeQuery();
                String starHtml = "";
                while (starResultSet.next()) {
                    String starName = starResultSet.getString("name");
                    String starId = starResultSet.getString("starId");

                    starHtml += "<a href=single-star.html?starId=" + starId + ">" + starName + "</a><br>";
                }
                starResultSet.close();

                genreStatement.setString(1, movieId);
                ResultSet genreResultSet = genreStatement.executeQuery();
                String genreHtml = "";
                while (genreResultSet.next()) {
                    String genreName = genreResultSet.getString("name");
                    genreId = genreResultSet.getString("genreId");

                    genreHtml += "<a href=movies.html?genreId=" + genreId + ">" + genreName + "</a><br>";
                }
                genreResultSet.close();

                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_rating", movieRating);
                jsonObject.addProperty("movie_stars", starHtml);
                jsonObject.addProperty("movie_genres", genreHtml);

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
            e.printStackTrace();
            
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}

