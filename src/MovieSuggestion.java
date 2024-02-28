import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet("/movie-suggestion")
public class MovieSuggestion extends HttpServlet {
	/*
	 * populate the Super hero hash map.
	 * Key is hero ID. Value is hero name.
	 */

	 // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /*
     * 
     * Match the query against superheroes and return a JSON response.
     * 
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "id": 101 } },
     * 	{ "value": "Supergirl", "data": { "id": 113 } }
     * ]
     * 
     * The format is like this because it can be directly used by the 
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *   
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     * 
     * 
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try (Connection conn = dataSource.getConnection()){
			JsonArray jsonArray = new JsonArray();

			// get the query string from parameter
			String movie = request.getParameter("query");
			
			// return the empty json array if query is null or empty
			if (movie == null || movie.trim().isEmpty()) {
				response.getWriter().write(jsonArray.toString());
				return;
			}	

			String search = "";
			for (String word: movie.split(" ")){
				search += "+" + word + "* ";
			}

			String query = "SELECT id, title FROM movies WHERE MATCH title AGAINST (? IN BOOLEAN MODE) LIMIT 10";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, search);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String id = rs.getString("id");
				String title = rs.getString("title");

				jsonArray.add(generateJsonObject(id, title));
			}
			rs.close();
			ps.close();

			response.getWriter().write(jsonArray.toString());
		} catch (Exception e) {
			System.out.println(e);
			response.sendError(500, e.getMessage());
		}
	}
	
	/*
	 * Generate the JSON Object from hero to be like this format:
	 * {
	 *   "value": "Iron Man",
	 *   "data": { "id": 11 }
	 * }
	 * 
	 */
	private static JsonObject generateJsonObject(String id, String title) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", title);
		
		JsonObject additionalDataJsonObject = new JsonObject();
		additionalDataJsonObject.addProperty("id", id);
		
		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}


}