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
            String parameterPage = (String) request.getParameter("page");
            String parameterShow = (String) request.getParameter("show");
            String parameterSort = (String) request.getParameter("sortBy");

            String movieTitle;
            String movieYear;
            String movieDirector;
            String movieStar;
            String genreId;
            String sortBy;
            int show;
            String firstLetter;

            if (request.getParameterMap().isEmpty()) {
                // jump to main page
                movieTitle = (String) session.getAttribute("movieTitle");
                movieYear = (String) session.getAttribute("movieYear");
                movieDirector = (String) session.getAttribute("movieDirector");
                movieStar = (String) session.getAttribute("movieStar");
                firstLetter = (String) session.getAttribute("firstLetter");
                genreId = (String) session.getAttribute("genreId");
                sortBy = (String) session.getAttribute("sortBy");
                show = (int) session.getAttribute("show");
                page = (int) session.getAttribute("page");
            }
            else if (parameterPage != null && !parameterPage.equals("")) {
                page = Integer.valueOf(parameterPage);

                movieTitle = (String) session.getAttribute("movieTitle");
                movieYear = (String) session.getAttribute("movieYear");
                movieDirector = (String) session.getAttribute("movieDirector");
                movieStar = (String) session.getAttribute("movieStar");
                firstLetter = (String) session.getAttribute("firstLetter");
                genreId = (String) session.getAttribute("genreId");
                sortBy = (String) session.getAttribute("sortBy");
                show = Integer.parseInt(session.getAttribute("show").toString());

                session.setAttribute("page", page);
            }
            else if (parameterShow != null && !parameterShow.equals("")) {
                show = Integer.valueOf(parameterShow);

                movieTitle = (String) session.getAttribute("movieTitle");
                movieYear = (String) session.getAttribute("movieYear");
                movieDirector = (String) session.getAttribute("movieDirector");
                movieStar = (String) session.getAttribute("movieStar");
                firstLetter = (String) session.getAttribute("firstLetter");
                genreId = (String) session.getAttribute("genreId");
                sortBy = (String) session.getAttribute("sortBy");
                //page = Integer.parseInt(session.getAttribute("page").toString());

                session.setAttribute("show", show);
                session.setAttribute("page", 0);
            }
            else if (parameterSort != null && !parameterSort.equals("")) {
                sortBy = parameterSort;

                movieTitle = (String) session.getAttribute("movieTitle");
                movieYear = (String) session.getAttribute("movieYear");
                movieDirector = (String) session.getAttribute("movieDirector");
                movieStar = (String) session.getAttribute("movieStar");
                firstLetter = (String) session.getAttribute("firstLetter");
                genreId = (String) session.getAttribute("genreId");
                //page = Integer.parseInt(session.getAttribute("page").toString());
                show = Integer.parseInt(session.getAttribute("show").toString());

                session.setAttribute("sortBy", sortBy);
                session.setAttribute("page", 0);
            }
            else {
                // new search
                movieTitle = request.getParameter("title");
                movieYear = request.getParameter("year");
                movieDirector = request.getParameter("director");
                movieStar = request.getParameter("star");
                firstLetter = request.getParameter("firstLetter");
                genreId = request.getParameter("genreId");
                sortBy = request.getParameter("sort");
                show = 25;

                session.setAttribute("sortBy", sortBy);
                session.setAttribute("show", show);
                session.setAttribute("page", 0);
            }

            String query = "";
            PreparedStatement statement = null;


            if (genreId != null && !genreId.equals("")) {
                genreId = genreId.strip();
                session.setAttribute("genreId", genreId);
                session.removeAttribute("firstLetter");

                movieStar = "";
                query = "SELECT id,title,year,director,rating FROM movies LEFT JOIN ratings ON (movies.id=ratings.movieId) JOIN genres_in_movies AS gm ON (gm.movieId=movies.id) WHERE gm.genreId=? ORDER BY " + sortBy + " LIMIT " + Integer.toString(show+1) + " OFFSET " + Integer.toString(page * show);

                // Declare our statement
                statement = conn.prepareStatement(query);
                statement.setInt(1, Integer.valueOf(genreId));
            }
            else if (firstLetter != null && !firstLetter.equals("")) {
                session.setAttribute("firstLetter", firstLetter);
                session.removeAttribute("genreId");

                movieStar = "";

                if (firstLetter.equals("*")) {
                    query = "SELECT id,title,year,director,rating FROM movies LEFT JOIN ratings ON (movies.id=ratings.movieId) WHERE title REGEXP '^[^a-zA-Z0-9]+' ORDER BY " + sortBy + " LIMIT " + Integer.toString(show+1) + " OFFSET " + Integer.toString(page * show);
                    statement = conn.prepareStatement(query);
                }
                else {
                    query = "SELECT id,title,year,director,rating FROM movies LEFT JOIN ratings ON (movies.id=ratings.movieId) WHERE title LIKE ? ORDER BY " + sortBy + " LIMIT " + Integer.toString(show+1) + " OFFSET " + Integer.toString(page * show);
                    statement = conn.prepareStatement(query);
                    statement.setString(1, firstLetter + "%");
                }
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
                session.removeAttribute("genreId");
                session.removeAttribute("firstLetter");

                //String search = " title LIKE ? AND director LIKE ? ";
                String search = " ";
                if (movieTitle != "") {
                    search += "title LIKE ? ";
                }
                if (movieDirector != "") {
                    if (!search.equals(" ")) {
                        search += "AND ";
                    }
                    search += "director LIKE ? ";
                }
                if (movieYear != "") {
                    if (!search.equals(" ")) {
                        search += "AND ";
                    }
                    search += "year=? ";
                }
                query = "SELECT id,title,year,director,rating FROM movies LEFT JOIN ratings ON (movies.id=ratings.movieId) WHERE" + search + "ORDER BY " + sortBy + " LIMIT " + Integer.toString(show+1) + " OFFSET " + Integer.toString(page * show);

                if (movieStar!=null && !movieStar.equals("")) {
                    search += "AND stars.name LIKE ? ";
                    query = "SELECT DISTINCT sm.movieId,title,year,director,rating from movies, ratings, stars, stars_in_movies AS sm WHERE sm.movieId=movies.id AND movies.id=ratings.movieId AND stars.id=sm.starId" + search + "ORDER BY " + sortBy + " LIMIT " + Integer.toString(show+1) + " OFFSET " + Integer.toString(page * show);
                }

                // Declare our statement
                statement = conn.prepareStatement(query);

                // Set parameters
                int index = 1;
                if (movieTitle!=null && !movieTitle.equals("")) {
                    statement.setString(1, "%" + movieTitle + "%");
                    index += 1;
                }
                if (movieDirector!=null && !movieDirector.equals("")) {
                    statement.setString(2, "%" + movieDirector + "%");
                    index += 1;
                }
                //statement.setString(1, "%" + movieTitle + "%");
                //statement.setString(2, "%" + movieDirector + "%");
                //int index = 3;
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

            String starQuery = "SELECT starId,name FROM stars, stars_in_movies WHERE starId IN (SELECT starId FROM stars_in_movies WHERE movieId=?) AND starId=id GROUP BY starId ORDER BY count(starId) DESC, name ASC LIMIT 3";
            //String starQuery = "SELECT starId, name FROM stars_in_movies INNER JOIN stars ON starId=id WHERE movieId=? GROUP BY starId ORDER BY count(starId) DESC, name ASC LIMIT 3";
            PreparedStatement starStatement = conn.prepareStatement(starQuery);

            String genreQuery = "SELECT name, genreId FROM genres, genres_in_movies AS gm WHERE movieId=? AND id=gm.genreId ORDER BY name LIMIT 3";
            PreparedStatement genreStatement = conn.prepareStatement(genreQuery);
            // Iterate through each row of rs
            while (rs.next()) {
                String movieId = rs.getString("id");
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

