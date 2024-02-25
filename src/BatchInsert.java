import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.sql.SQLException;


public class BatchInsert {
    private List<Movie> movieList = null;
    private List<StarInMovie> starList = null;

    private Connection conn = null;
    public BatchInsert(List<Movie> movieList, List<StarInMovie> starList) throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        String jdbcURL="jdbc:mysql://localhost:3306/moviedb";

        this.movieList = movieList;
        this.starList = starList;

        try {
            conn = DriverManager.getConnection(jdbcURL,"mytestuser", "testUser1");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertData() {
        int[] iNoRows = null;

        // insert movies
        PreparedStatement psInsertMovie = null;
        PreparedStatement psInsertGenre = null;
        PreparedStatement psInsertGenreInMovie = null;
        PreparedStatement psGenreExists = null;
        
        String sqlInsertMovie = "INSERT INTO movies(id, title, year, director) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE id=id";

        String sqlGetGenreId = "SELECT MAX(id) AS id FROM genres";
        String sqlGenreExists = "SELECT id FROM genres WHERE name=?";
        String sqlInsertGenre = "INSERT INTO genres(id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE id=id";
        String sqlInsertGenreInMovie = "INSERT INTO genres_in_movies(genreId, movieId) VALUES (?, ?)";

        try {
            PreparedStatement psGetGenreId = conn.prepareStatement(sqlGetGenreId);
            ResultSet rs = psGetGenreId.executeQuery();
            rs.next();
            int maxGenreId = Integer.valueOf(rs.getString("id"))+1;
            rs.close();
            psGetGenreId.close(); 

			conn.setAutoCommit(false);

            psInsertMovie = conn.prepareStatement(sqlInsertMovie);
            psGenreExists = conn.prepareStatement(sqlGenreExists);
            psInsertGenre = conn.prepareStatement(sqlInsertGenre);
            psInsertGenreInMovie = conn.prepareStatement(sqlInsertGenreInMovie);
            
            int i = 0;
            int genreId;
            for(Movie movie: movieList)
            {
                psInsertMovie.setString(1, movie.getId());
                psInsertMovie.setString(2, movie.getTitle());
                psInsertMovie.setInt(3, movie.getYear());
                psInsertMovie.setString(4, movie.getDirector());
                psInsertMovie.addBatch();

                for (String genre:movie.getGenres()) {
                    psGenreExists.setString(1, genre.trim());
                    rs = psGenreExists.executeQuery();
                    try {
                        rs.next();
                        genreId = rs.getInt("id");
                    }
                    catch (Exception e) {
                        genreId = maxGenreId;
                        maxGenreId++;
                    }
                    rs.close();
                    psInsertGenre.setInt(1, genreId);
                    psInsertGenre.setString(2, genre.trim());
                    psInsertGenre.addBatch();

                    psInsertGenreInMovie.setInt(1, genreId);
                    psInsertGenreInMovie.setString(2, movie.getId());
                    psInsertGenreInMovie.addBatch();
                }

                if (i == 60) {
                    iNoRows = psInsertMovie.executeBatch();
                    psInsertGenre.executeBatch();
                    psInsertGenreInMovie.executeBatch();
       
                    i = 0;        
                }
                else {
                    i++;
                }
            }
			iNoRows = psInsertMovie.executeBatch();
            psInsertGenre.executeBatch();
            psInsertGenreInMovie.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Done with movie batch");


        // insert stars
        PreparedStatement psInsertStar = null;
        PreparedStatement psInsertStarInMovie = null;
        PreparedStatement psGetStarId = null;
        PreparedStatement psStarExists = null;

        String sqlGetStarId = "SELECT MAX(id) AS id FROM stars";
        String sqlStarExists = "SELECT id FROM stars WHERE name=? AND birthYear IS null";
        String sqlInsertStar = "INSERT INTO stars(id, name, birthYear) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE id=id";
        String sqlInsertStarInMovie = "INSERT INTO stars_in_movies(starId, movieId) VALUES (?, ?)";

        try {
            psGetStarId = conn.prepareStatement(sqlGetStarId);
            psStarExists = conn.prepareStatement(sqlStarExists);

            ResultSet rs = psGetStarId.executeQuery();
            rs.next();
            int maxStarId = Integer.valueOf(rs.getString("id").substring(2))+1;
            rs.close();
            psGetStarId.close();

            psInsertStar = conn.prepareStatement(sqlInsertStar);
            psInsertStarInMovie = conn.prepareStatement(sqlInsertStarInMovie);
            
            int i = 0;
            for(StarInMovie star: starList) {
                psStarExists.setString(1, star.getStarName());
                rs = psStarExists.executeQuery();

                String starId;
                try {
                    rs.next();
                    starId = rs.getString("id");
                }
                catch (Exception e) {
                    starId = "nm" + String.valueOf(maxStarId);
                    maxStarId++;

                    psInsertStar.setString(1, starId);
                    psInsertStar.setString(2, star.getStarName());
                    psInsertStar.setNull(3, java.sql.Types.INTEGER);
                    psInsertStar.addBatch();
                }
                rs.close();

                psInsertStarInMovie.setString(1, starId);
                psInsertStarInMovie.setString(2, star.getFilmId());
                psInsertStarInMovie.addBatch();

                maxStarId++;

                if (i == 60) {
                    iNoRows = psInsertStar.executeBatch();
                    psInsertStarInMovie.executeBatch();
         
                    i = 0;        
                }
                else {
                    i++;
                }
            }
            iNoRows = psInsertStar.executeBatch();
            psInsertStarInMovie.executeBatch();    

        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        System.out.println("Done with stars batch");
        try {
            conn.commit();     

            if(psInsertMovie!=null) psInsertMovie.close();
            if(psInsertGenre!=null) psInsertGenre.close();
            if(psInsertGenreInMovie!=null) psInsertGenreInMovie.close();
            if(psGenreExists!=null) psGenreExists.close();
            
            if(psInsertStar!=null) psInsertStar.close();
            if(psInsertStarInMovie!=null) psInsertStarInMovie.close();
            if(psGetStarId!=null) psGetStarId.close();
            if(psStarExists!=null)  psStarExists.close();

            psGenreExists.close();
            
            if(conn!=null) conn.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}


