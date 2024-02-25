import java.util.ArrayList;

public class Movie {

    private final String title;

    private final int year;

    private final String director;

    private final String id;

    private final ArrayList<String> genres;

    public Movie(String id, String title, String director, int year, ArrayList<String> genres) {
        this.title = title.trim();
        this.director = director.trim();
        this.year = year;
        this.id = id.trim();
        this.genres = genres;
    }

    public int getYear() {
        return year;
    }


    public String getTitle() {
        return title;
    }

    public String getDirector() {
        return director;
    }

    public String getId() {
        return id;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public String toString() {

        return "Year:" + getYear() + ", " +
                "Title:" + getTitle() + ", " +
                "Director:" + getDirector() +  ".";
    }
}
