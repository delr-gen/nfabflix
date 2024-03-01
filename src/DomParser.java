import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DomParser {

    List<Movie> movies = new ArrayList<>();
    List<StarInMovie> stars = new ArrayList<>();
    Document dom;

    public void run() {
        try {
            // parse the xml file and get the dom object
            BatchInsert batchInsert = new BatchInsert();

            parseXmlFile("xml/mains243.xml");
            parseMovieDocument();
            System.out.println("Done parsing mains243.xml");
            batchInsert.insertMovies(movies);
            System.out.println("Finished inserting movies");

            parseXmlFile("xml/casts124.xml");
            parseStarDocument();
            System.out.println("Done parsing casts124.xml");
            batchInsert.insertStars(stars);
            System.out.println("Finished inserting stars");

            batchInsert.closeConnection();
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }
    }

    private void parseXmlFile(String fileName) {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse(fileName);

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseMovieDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        // get a nodelist of employee Elements, parse each into Employee object
        NodeList nodeDirectorFilmsList = documentElement.getElementsByTagName("directorfilms"); // film
        for (int i = 0; i < nodeDirectorFilmsList.getLength(); i++) {
            // get the directorfilm element
            Element directorFilmElement = (Element) nodeDirectorFilmsList.item(i);

            // get all film nodes within directorfilm element
            NodeList nodeFilmList = directorFilmElement.getElementsByTagName("film");

            // get director name from directorfilm element
            String director = "";
            try {
                director = getTextValue(directorFilmElement, "dirname");
            }
            catch (Exception e) {
                continue;
            }

            // iterate through all film nodes under directorfilm element
            for (int j = 0; j < nodeFilmList.getLength(); j++){
                // convert film node to an element and parse it
                Element filmElement = (Element) nodeFilmList.item(j);
                Movie movie = parseMovie(director, filmElement);

                // add it to list
                if (movie != null) {
                    movies.add(movie);
                }
            }
        }
    }


    private void parseStarDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement();

        NodeList nodeFilmcList = documentElement.getElementsByTagName("m");
        for (int i=0; i < nodeFilmcList.getLength(); i++) {
            Element m = (Element) nodeFilmcList.item(i);
            StarInMovie sm = parseStar(m);

            if (sm != null) {
                stars.add(sm);
            }
        }
    }

    /**
     * It takes an employee Element, reads the values in, creates
     * an Employee object for return
     */
    private Movie parseMovie(String director, Element element) {

        ArrayList<String> genres = getMultipleTextValue(element, "cat");
        try {
            int year = Integer.parseInt(getTextValue(element, "year")); // movie year;
            String title = getTextValue(element, "t"); // movie name
            String id = getTextValue(element, "fid");

            return new Movie(id, title, director, year, genres);
        }
        catch  (Exception e) {
            // if invalid values, write to a file
            return null;
        }

        // create a new Employee with the value read from the xml nodes
        //return new Movie(name, id, age, type);
    }


    private StarInMovie parseStar(Element element) {
        try {
            String filmId = getTextValue(element, "f");
            String starName = getTextValue(element, "a");

            return new StarInMovie(starName, filmId);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * It takes an XML element and the tag name, look for the tag and get
     * the text content
     * i.e for <Employee><Name>John</Name></Employee> xml snippet if
     * the Element points to employee node and tagName is name it will return John
     */
    private String getTextValue(Element element, String tagName) throws Exception {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);

        if (nodeList.getLength() > 0) {
            // here we expect only one <Name> would present in the <Employee>
            textVal = nodeList.item(0).getFirstChild().getNodeValue();
        }

        if (textVal == null) {
            throw new Exception();
        }

        return textVal;
    }

    private ArrayList<String> getMultipleTextValue(Element element, String tagName) {
        ArrayList<String> values = new ArrayList<String>();
        String textVal = null;

        NodeList nodesList = element.getElementsByTagName(tagName); // dirn
        for (int i = 0; i < nodesList.getLength(); i++) {
            Node node = nodesList.item(i);
            try {
                textVal = node.getFirstChild().getNodeValue();
                if (textVal != null) {
                    values.add(textVal);
                }
            }
            catch (Exception e) {
                continue;
            }
        }
        return values;

    }

    public static void main(String[] args) {
        // create an instance
        DomParser domParser = new DomParser();

        // run Dom Parser
        domParser.run();
    }

}
