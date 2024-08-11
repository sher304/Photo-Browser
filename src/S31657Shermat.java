import javax.swing.*;


public class S31657Shermat {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> photoBrowser());
    }

    public static void photoBrowser() {
        PhotoBrowser photoBrowser = new PhotoBrowser();
        photoBrowser.setVisible(true);
    }
}
