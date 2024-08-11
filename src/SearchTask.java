import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.regex.Pattern;

public class SearchTask extends SwingWorker<Void, Photo> {
    private List<Photo> photos;
    private String searchText;
    private boolean useAndLogic;
    private DefaultTableModel tableModel;

    public SearchTask(List<Photo> photos, String searchText, boolean useAndLogic, DefaultTableModel tableModel) {
        this.photos = photos;
        this.searchText = searchText;
        this.useAndLogic = useAndLogic;
        this.tableModel = tableModel;
    }

    @Override
    protected Void doInBackground() {
        for (Photo photo : photos) {
            if (matchesCriteria(photo, searchText, useAndLogic)) {
                publish(photo);
            }
        }
        return null;
    }

    @Override
    protected void process(List<Photo> chunks) {
        for (Photo photo : chunks) {
            ImageIcon cellIcon = createScaledImageIcon(photo.getFilePath(), 40, 100);
            tableModel.addRow(new Object[]{cellIcon, photo.getTitle(), photo.getDate(), photo.getTags(), photo.getDescription(), photo.getCollection()});
        }
    }

    private boolean matchesCriteria(Photo photo, String searchText, boolean useAndLogic) {
        String searchLowerCase = searchText.toLowerCase().trim();
        boolean matches = true;

        if (!searchText.isEmpty()) {
            boolean titleMatch = photo.getTitle().toLowerCase().contains(searchLowerCase);
            boolean collectionMatch = photo.getCollection().toLowerCase().contains(searchLowerCase);
            boolean descriptionMatch = photo.getDescription().toLowerCase().matches(".*\\b" + Pattern.quote(searchLowerCase) + "\\b.*");
            boolean tagsMatch = photo.getTags().stream()
                    .anyMatch(tag -> tag.toLowerCase().contains(searchLowerCase));

            if (useAndLogic) {
                matches = titleMatch && descriptionMatch && tagsMatch && collectionMatch;
            } else {
                matches = titleMatch || descriptionMatch || tagsMatch || collectionMatch;
            }
        }
        return matches;
    }

    private ImageIcon createScaledImageIcon(String path, int width, int height) {
        // Implement your image scaling logic here
        // This is a placeholder implementation
        return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH));
    }
}
