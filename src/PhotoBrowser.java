import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.table.JTableHeader;
import java.util.*;
import java.util.List;

public class PhotoBrowser extends JFrame {

    private DefaultTableModel tableModel;
    private DefaultListModel<String> tagListModel;
    private DefaultListModel<String> collectionListModel;
    JTable photoTable;
    private List<Photo> photos; // Collection to store loaded photos
    private boolean dateSortAscending = true; // Track sort order
    private static final String tagsFile = "tags.txt";
    private static final String photosFile = "photos.txt";
    private static final String collectionsFile = "collections.txt";

    public PhotoBrowser() {
        photos = new ArrayList<>();

        // Set the title of the window
        setTitle("Photo Browser");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set the layout of the main panel
        setLayout(new BorderLayout());

        // Create the main panels
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        // Top left panel for image display and add photo button
        JPanel topLeftPanel = new JPanel(new BorderLayout());
        topLeftPanel.setPreferredSize(new Dimension(300, 250)); // Set preferred size
        JLabel imageLabel = new JLabel();
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel topLeftInnerDetails = new JPanel(new GridLayout(1, 2));
        JButton addPhotoButton = new JButton("Add Photo");

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));

        JTextField searchField = new JTextField(5);
        JButton searchButton = new JButton("Search");
        JTextField searchDescriptionField = new JTextField(5);
        JCheckBox andLogicCheckBox = new JCheckBox("AND Logic");

        JPanel searchFieldPanel = new JPanel(new GridLayout(4, 1));
        searchFieldPanel.add(new Label("Search (title/tag/collection)"));
        searchFieldPanel.add(searchField);
        searchFieldPanel.add(new Label("Search (description)"));
        searchFieldPanel.add(searchDescriptionField);

        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            boolean useAndLogic = andLogicCheckBox.isSelected();
            tableModel.setRowCount(0); // Clear the table
            photos.clear();
            loadPhotos();

            if (searchField.getText().isEmpty())  searchPhotos(searchDescriptionField.getText().trim(), useAndLogic);
            else searchPhotos(searchText, useAndLogic);
        });


        searchPanel.add(andLogicCheckBox, BorderLayout.EAST);

        searchPanel.add(searchFieldPanel);
        searchPanel.add(searchButton, BorderLayout.SOUTH);

        topLeftInnerDetails.add(imageLabel);
        topLeftInnerDetails.add(searchPanel);

        topLeftPanel.add(topLeftInnerDetails, BorderLayout.CENTER);
        topLeftPanel.add(addPhotoButton, BorderLayout.SOUTH);

        // Table for displaying photo details
        String[] columnNames = {"", "Title", "Date", "Tags", "Description", "Collections"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make the first column (icon column) non-editable
                return column != 0;
            }
        };

        photoTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(photoTable);
        photoTable.getColumnModel().getColumn(0).setCellRenderer(new CustomTableCell());
        photoTable.getColumnModel().getColumn(0).setPreferredWidth(40); // Adjust column width for images
        photoTable.setRowHeight(100); // Adjust row height for images
        photoTable.getColumnModel().getColumn(0).setCellRenderer(new CustomTableCell()); // Custom renderer for images
        photoTable.setFillsViewportHeight(true);

        JTableHeader tableHeader = photoTable.getTableHeader();
        tableHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = photoTable.columnAtPoint(e.getPoint());
                if (col == 2) {
                    sortPhotosByDate();
                }
            }
        });

        // Create popup menu for adding to collection
        JPopupMenu collectionMenu = new JPopupMenu();
        JMenuItem addToCollectionItem = new JMenuItem("Add to Collection");
        addToCollectionItem.addActionListener(e -> showAddCollectionDialog(photoTable.getSelectedRow()));
        collectionMenu.add(addToCollectionItem);

        // Right-click menu for photo table
        photoTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = photoTable.rowAtPoint(e.getPoint());
                    int column = photoTable.columnAtPoint(e.getPoint());
                    if (column >= 0 && column <= 5) {
                        photoTable.setRowSelectionInterval(row, row);
                        photoTable.setColumnSelectionInterval(column, column);
                        collectionMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    System.out.println("CLICKEED!");
                    int selectedRow = photoTable.getSelectedRow();
                    if (selectedRow != -1) {
                        Photo selectedPhoto = photos.get(selectedRow);
                        System.out.println(selectedPhoto.getTitle());
                        ImageIcon imageIcon = createScaledImageIcon(selectedPhoto.getFilePath(), imageLabel.getWidth(), imageLabel.getHeight());
                        imageLabel.setIcon(imageIcon);
                    }
                }
            }
        });

        // Left panel containing top left panel and table
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(topLeftPanel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // Tags panel
        JPanel tagsPanel = new JPanel(new BorderLayout());
        tagsPanel.setBorder(BorderFactory.createTitledBorder("Tags"));
        tagListModel = new DefaultListModel<>();
        loadTags();

        JList<String> tagList = new JList<>(tagListModel);
        JButton addTagButton = new JButton("+");
        tagList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    String selectedTag = tagList.getSelectedValue();
                    if (selectedTag != null) {
                        filterPhotosByTag(selectedTag);
                    }
                }
            }
        });

        tagsPanel.add(new JScrollPane(tagList), BorderLayout.CENTER);
        tagsPanel.add(addTagButton, BorderLayout.SOUTH);

        // Popup menu for adding to tags
        JPopupMenu tagsMenu = new JPopupMenu();
        JMenuItem addToTagsMenuItem = new JMenuItem("Add to Tags");
        addToTagsMenuItem.addActionListener(e -> showAddTagsDialog());
        tagsMenu.add(addToTagsMenuItem);

        addTagButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                tagsMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        // Collections panel
        JPanel collectionsPanel = new JPanel(new BorderLayout());
        collectionsPanel.setBorder(BorderFactory.createTitledBorder("Collections"));
        collectionListModel = new DefaultListModel<>(); // Initialize collection list model

        JList<String> collectionList = new JList<>(collectionListModel);
        collectionsPanel.add(new JScrollPane(collectionList), BorderLayout.CENTER);
        loadCollections();

        // Add mouse listener to the collection list
        collectionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    String selectedCollection = collectionList.getSelectedValue();
                    if (selectedCollection != null) {
                        filterPhotosByCollection(selectedCollection);
                    }
                }
            }
        });

        // Add photo button action listener
        addPhotoButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser("user.home");
            int returnValue = fileChooser.showOpenDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                ImageIcon imageIcon = createScaledImageIcon(selectedFile.getPath(), imageLabel.getWidth(), imageLabel.getHeight());
                imageLabel.setIcon(imageIcon);
                showInformationDialog(selectedFile);
            }
        });

        // Add panels to the right panel
        rightPanel.add(tagsPanel);
        rightPanel.add(collectionsPanel);

        // Add main panels to the frame
        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Load photos on startup
        loadPhotos();
    }

    private void showInformationDialog(File selectedFile) {
        new PhotoInformationDialog(this, selectedFile);
    }

    public DefaultListModel<String> getTagListModel() {
        return tagListModel;
    }

    public DefaultListModel<String> getCollectionListModel() {
        return collectionListModel;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public void savePhotoToFile(String imageIcon, String title, String date, Set<String> tags, String description, String collections, File selectedFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(photosFile, true))) {
            String tagsStr = String.join(",", tags);
            writer.println(imageIcon + "|" + title + "|" + date + "|" + tagsStr + "|" + description + "|" + collections);
        } catch (IOException e) {
            System.out.println("Error saving photo: " + e.getMessage());
        }
    }

    // load photo
    private void loadPhotos() {
        File file = new File(photosFile);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 6) {
                        String filePath = parts[0];
                        String title = parts[1];
                        String date = parts[2];
                        Set<String> tags = new HashSet<>(Arrays.asList(parts[3].split(","))); // Convert comma-separated String to Set<String>
                        String description = parts[4];
                        String collections = parts[5];
                        Photo photo = new Photo(filePath, title, date, tags, description, collections);
                        ImageIcon cellIcon = createScaledImageIcon(parts[0], 40, 100);
                        tableModel.addRow(new Object[]{cellIcon, title, date, tags, description, collections});
                        photos.add(photo);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading photos: " + e.getMessage());
            }
        }
    }

    private void searchPhotos(String searchText, boolean useAndLogic) {
        DefaultTableModel model = (DefaultTableModel) photoTable.getModel();
        model.setRowCount(0);

        SearchTask searchTask = new SearchTask(photos, searchText, useAndLogic, model);
        searchTask.execute();
    }

    public void saveTagsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(tagsFile))) {
            for (int i = 0; i < tagListModel.size(); i++) {
                writer.println(tagListModel.get(i));
            }
        } catch (IOException e) {
            System.out.println("Error saving tags: " + e.getMessage());
        }
    }

    public void saveCollectionToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(collectionsFile))) {
            for (int i = 0; i < collectionListModel.size(); i++) {
                writer.println(collectionListModel.get(i));
            }
        } catch (IOException e) {
            System.out.println("Error saving tags: " + e.getMessage());
        }
    }

    public boolean tagExists(String tagName) {
        for (int i = 0; i < tagListModel.size(); i++) {
            if (tagName.equalsIgnoreCase(tagListModel.getElementAt(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean collectionExists(String collectionName) {
        for (int i = 0; i < collectionListModel.size(); i++) {
            if (collectionName.equalsIgnoreCase(collectionListModel.getElementAt(i))) {
                return true;
            }
        }
        return false;
    }

    private void loadTags() {
        File file = new File(tagsFile);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    tagListModel.addElement(line);
                }
            } catch (IOException e) {
                System.out.println("Error loading tags: " + e.getMessage());
            }
        } else {
            // Initial loading of default tags/categories if the file doesn't exist
            for (Tag category : EnumSet.allOf(Tag.class)) {
                tagListModel.addElement(category.toString());
            }
        }
    }

    private void loadCollections() {
        File file = new File(collectionsFile);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    collectionListModel.addElement(line);
                }
            } catch (IOException e) {
                System.out.println("Error loading collections: " + e.getMessage());
            }
        }
    }

    private void showAddCollectionDialog(int selectedRow) {
        String collectionName = JOptionPane.showInputDialog(this, "Enter collection name:");
        if (collectionName != null && !collectionName.trim().isEmpty()) {
            collectionListModel.addElement(collectionName);
            addSelectedPhotoToCollection(selectedRow, collectionName);
            saveCollectionToFile();
        }
    }

    private void addSelectedPhotoToCollection(int selectedRow, String collectionName) {
        if (selectedRow != -1) {
            String currentCollections = (String) tableModel.getValueAt(selectedRow, 5);
            if (currentCollections == null || currentCollections.trim().isEmpty()) {
                tableModel.setValueAt(collectionName, selectedRow, 5);
            } else {
                tableModel.setValueAt(currentCollections + ", " + collectionName, selectedRow, 5);
            }
        }
    }

    private void showAddTagsDialog() {
        String tagName = JOptionPane.showInputDialog(this, "Enter tag name:");
        if (tagName != null && !tagName.trim().isEmpty()) {
            // Check if the tag already exists
            if (!tagExists(tagName)) {
                // Add to the tag list model
                tagListModel.addElement(tagName);
                saveTagsToFile();
            } else {
                JOptionPane.showMessageDialog(this, "Tag already exists.", "Duplicate Tag", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public ImageIcon createScaledImageIcon(String path, int width, int height) {
        ImageIcon originalIcon = new ImageIcon(path);
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private void filterPhotosByTag(String tag) {
        DefaultTableModel model = (DefaultTableModel) photoTable.getModel();
        model.setRowCount(0);

        for (Photo photo : photos) {
            if (photo.getTags().contains(tag)) {
                ImageIcon cellIcon = createScaledImageIcon(photo.getFilePath(), 40, 100);
                model.addRow(new Object[]{cellIcon, photo.getTitle(), photo.getDate(), photo.getTags(), photo.getDescription(), photo.getCollection()});
            }
        }
    }

    private void filterPhotosByCollection(String collection) {
        DefaultTableModel model = (DefaultTableModel) photoTable.getModel();
        model.setRowCount(0); // Clear table

        for (Photo photo : photos) {
            if (photo.getCollection().contains(collection)) {
                ImageIcon cellIcon = createScaledImageIcon(photo.getFilePath(), 40, 100);
                model.addRow(new Object[]{cellIcon, photo.getTitle(), photo.getDate(), photo.getTags(), photo.getDescription(), photo.getCollection()});
            }
        }
    }

    private void updatePhotoTable() {
        tableModel.setRowCount(0);
        for (Photo photo : photos) {
            ImageIcon icon = createScaledImageIcon(photo.getFilePath(), 50, 50);
            tableModel.addRow(new Object[]{icon, photo.getTitle(), photo.getDate(), photo.getTags(), photo.getDescription(), photo.getCollection()});
        }
    }

    private void sortPhotosByDate() {
        Collections.sort(photos, new Comparator<Photo>() {
            @Override
            public int compare(Photo p1, Photo p2) {
                Date date1 = parseDate(p1.getDate());
                Date date2 = parseDate(p2.getDate());
                if (date1 != null && date2 != null) {
                    return dateSortAscending ? date1.compareTo(date2) : date2.compareTo(date1);
                }
                return 0;
            }
        });
        dateSortAscending = !dateSortAscending;
        updatePhotoTable();
    }

    private Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}