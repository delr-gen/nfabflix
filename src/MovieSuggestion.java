import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.DirStateFactory.Result;
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

	public static HashMap<Integer, String> superHeroMap = new HashMap<>();
	
	// static {
	// 	superHeroMap.put(1, "Blade");
	// 	superHeroMap.put(2, "Ghost Rider");
	// 	superHeroMap.put(3, "Luke Cage");
	// 	superHeroMap.put(4, "Silver Surfer");
	// 	superHeroMap.put(5, "Beast");
	// 	superHeroMap.put(6, "Thing");
	// 	superHeroMap.put(7, "Black Panther");
	// 	superHeroMap.put(8, "Invisible Woman");
	// 	superHeroMap.put(9, "Nick Fury");
	// 	superHeroMap.put(10, "Storm");
	// 	superHeroMap.put(11, "Iron Man");
	// 	superHeroMap.put(12, "Professor X");8
	// 	superHeroMap.put(13, "Hulk");
	// 	superHeroMap.put(14, "Cyclops");
	// 	superHeroMap.put(15, "Thor");
	// 	superHeroMap.put(16, "Jean Grey");
	// 	superHeroMap.put(17, "Wolverine");
	// 	superHeroMap.put(18, "Daredevil");
	// 	superHeroMap.put(19, "Captain America");
	// 	superHeroMap.put(20, "Spider-Man");
	// 	superHeroMap.put(101, "Superman");
	// 	superHeroMap.put(102, "Batman");
	// 	superHeroMap.put(103, "Wonder Woman");
	// 	superHeroMap.put(104, "Flash");
	// 	superHeroMap.put(105, "Green Lantern");
	// 	superHeroMap.put(106, "Catwoman");
	// 	superHeroMap.put(107, "Nightwing");
	// 	superHeroMap.put(108, "Captain Marvel");
	// 	superHeroMap.put(109, "Aquaman");
	// 	superHeroMap.put(110, "Green Arrow");
	// 	superHeroMap.put(111, "Martian Manhunter");
	// 	superHeroMap.put(112, "Batgirl");
	// 	superHeroMap.put(113, "Supergirl");
	// 	superHeroMap.put(114, "Black Canary");
	// 	superHeroMap.put(115, "Hawkgirl");
	// 	superHeroMap.put(116, "Cyborg");
	// 	superHeroMap.put(117, "Robin");
	// }

    /*
     * 
     * Match the query against superheroes and return a JSON response.
     * 
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "heroID": 101 } },
     * 	{ "value": "Supergirl", "data": { "heroID": 113 } }
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
	 *   "data": { "heroID": 11 }
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