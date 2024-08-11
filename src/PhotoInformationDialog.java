import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PhotoInformationDialog extends JDialog {
    private JTextField titleField;
    private JTextField dateField;
    private JTextField tagField;
    private JTextField descriptionField;
    private JTextField collectionsField;
    private DefaultListModel<String> tagListModel;
    private DefaultListModel<String> collectionListModel;
    private File selectedFile;
    private PhotoBrowser photoBrowser;

    public PhotoInformationDialog(PhotoBrowser photoBrowser, File selectedFile) {
        super(photoBrowser, "Photo Details", true);
        this.photoBrowser = photoBrowser;
        this.selectedFile = selectedFile;
        this.tagListModel = photoBrowser.getTagListModel();
        this.collectionListModel = photoBrowser.getCollectionListModel();

        initializeDialog();
    }

    private void initializeDialog() {
        setSize(700, 300); // Adjusted size for better layout
        setLayout(new GridBagLayout());

        // Panel for detail entry
        JPanel entryPanel = new JPanel(new GridLayout(6, 2));
        entryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Components for fields entry
        ImageIcon scaledImageIcon = photoBrowser.createScaledImageIcon(selectedFile.getPath(), 150, 200);

        JLabel imagePreview = new JLabel(scaledImageIcon);
        imagePreview.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 6;
        gbc.fill = GridBagConstraints.VERTICAL;
        add(imagePreview, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);

        entryPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        entryPanel.add(titleField);

        entryPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        entryPanel.add(dateField);

        entryPanel.add(new JLabel("Tags (comma separated):"));
        tagField = new JTextField();
        entryPanel.add(tagField);

        entryPanel.add(new JLabel("Description:"));
        descriptionField = new JTextField();
        entryPanel.add(descriptionField);

        entryPanel.add(new JLabel("Collections (comma separated):"));
        collectionsField = new JTextField();
        entryPanel.add(collectionsField);

        gbc.gridwidth = 2;
        add(entryPanel, gbc);

        // Create and configure JComboBox for tags
        JComboBox<String> tagsComboBox = new JComboBox<>();
        for (int i = 0; i < tagListModel.size(); i++) {
            tagsComboBox.addItem(tagListModel.getElementAt(i));
        }

        // Add action listener to tag field to show JComboBox on click
        tagField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = tagField.getLocationOnScreen().x;
                int y = tagField.getLocationOnScreen().y + tagField.getHeight();
                tagsComboBox.setPopupVisible(true);
                tagsComboBox.showPopup();
                tagsComboBox.setLocation(x, y);
            }
        });

        // Add action listener to JComboBox to update tag field with selected item
        tagsComboBox.addActionListener(e -> {
            String selectedTag = (String) tagsComboBox.getSelectedItem();
            if (selectedTag != null && !selectedTag.trim().isEmpty()) {
                String currentTags = tagField.getText().trim();
                if (currentTags.isEmpty()) {
                    tagField.setText(selectedTag);
                } else {
                    tagField.setText(currentTags + ", " + selectedTag);
                }
            }
        });

        entryPanel.add(tagsComboBox);

        // Create and configure JComboBox for collections
        JComboBox<String> collectionsComboBox = new JComboBox<>();
        for (int i = 0; i < collectionListModel.size(); i++) {
            collectionsComboBox.addItem(collectionListModel.getElementAt(i));
        }

        // Add action listener to collections field to show JComboBox on click
        collectionsField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = collectionsField.getLocationOnScreen().x;
                int y = collectionsField.getLocationOnScreen().y + collectionsField.getHeight();
                collectionsComboBox.setPopupVisible(true);
                collectionsComboBox.showPopup();
                collectionsComboBox.setLocation(x, y);
            }
        });

        // Add action listener to JComboBox to update collections field with selected item
        collectionsComboBox.addActionListener(e -> {
            String selectedCollection = (String) collectionsComboBox.getSelectedItem();
            if (selectedCollection != null && !selectedCollection.trim().isEmpty()) {
                String currentCollections = collectionsField.getText().trim();
                if (currentCollections.isEmpty()) {
                    collectionsField.setText(selectedCollection);
                } else {
                    collectionsField.setText(currentCollections + ", " + selectedCollection);
                }
            }
        });

        entryPanel.add(collectionsComboBox);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            savePhotoDetails();
        });

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(saveButton, gbc);

        // Set dialog location relative to main frame
        setLocationRelativeTo(photoBrowser);

        // Show dialog
        setVisible(true);
    }

    private void savePhotoDetails() {
        String title = titleField.getText();
        String date = dateField.getText();
        Set<String> tags = new HashSet<>(Arrays.asList(tagField.getText().split(",")));
        String description = descriptionField.getText();
        String collections = collectionsField.getText();

        // Add new tags and collections to their respective models if they do not exist
        for (String tag : tags) {
            if (!photoBrowser.tagExists(tag.trim())) {
                tagListModel.addElement(tag.trim());
            }
        }
        photoBrowser.saveTagsToFile();

        for (String collection : collections.split(",")) {
            String trimmedCollection = collection.trim();
            if (!photoBrowser.collectionExists(trimmedCollection)) {
                collectionListModel.addElement(trimmedCollection);
            }
        }
        photoBrowser.saveCollectionToFile();

        if (date.isEmpty()) {
            date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        ImageIcon cellIcon = photoBrowser.createScaledImageIcon(selectedFile.getPath(), 40, 100);
        photoBrowser.getTableModel().addRow(new Object[]{cellIcon, title, date, tags, description, collections});

        photoBrowser.savePhotoToFile(selectedFile.getPath(), title, date, tags, description, collections, selectedFile);
        dispose(); // Close dialog
    }
}
