import java.util.Set;

public class Photo {
    private String filePath;
    private String title;
    private String date;
    private Set<String> tags;
    private String description;
    private String collection;

    public Photo(String filePath, String title, String date, Set<String> tags, String description, String collection) {
        this.filePath = filePath;
        this.title = title;
        this.date = date;
        this.tags = tags;
        this.description = description;
        this.collection = collection;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public Set<String> getTags() {
        return tags;
    }

    public String getDescription() {
        return description;
    }

    public String getCollection() {
        return collection;
    }
}
