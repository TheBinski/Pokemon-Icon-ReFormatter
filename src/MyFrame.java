import java.util.Arrays;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.Graphics2D;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MyFrame extends JFrame implements ActionListener {
 
    JButton formatButton;
    JButton selectFileButton;
    JLabel errorText;
    JLabel selectedFile;

    File files[] = null;

    MyFrame() {

        Dimension buttonDim = new Dimension(100, 35);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout());
        this.setPreferredSize(new Dimension(200, 250));
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        selectedFile = new JLabel("No File(s) Selected");

        selectFileButton = new JButton("Select File");
        selectFileButton.setPreferredSize(buttonDim);
        selectFileButton.setFocusPainted(false);
        selectFileButton.addActionListener(this);

        formatButton = new JButton("Format File");
        formatButton.setPreferredSize(buttonDim);
        formatButton.addActionListener(this);
        formatButton.setFocusPainted(false);

        errorText = new JLabel("");
        errorText.setVisible(false);

        this.add(selectedFile);
        this.add(selectFileButton);
        this.add(formatButton);
        this.add(errorText);
        this.pack();
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == selectFileButton) {

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Files");
            fileChooser.setMultiSelectionEnabled(true);
            
            int response = fileChooser.showOpenDialog(null);

            if (response == JFileChooser.APPROVE_OPTION) {

                files = fileChooser.getSelectedFiles();

                if (files.length == 1) {

                    String filePath = files[0].toString();
                    String fileName = filePath.substring(filePath.lastIndexOf('\\')+1);
                    if (!fileName.contains(".png") && !fileName.contains(".jpg")) {
                        setErrorText("File must be either PNG or JPG format", true);
                        files = null;
                        selectedFile = new JLabel("No File(s) Selected");
                    } else {
                        String html = "<html><div style=\"padding:0px 25%;\">%s</div></html>";
                        selectedFile.setText(String.format(html, fileName));
                        // Makes sure that error text is not visible
                        hideErrorText();
                    }

                } else if (files.length > 1) {

                    String filePath;
                    String fileName;
                    int fileCount = 0;
                    Boolean incorrectFileTypesFound = false;
                    int[] incorrectFiles = {};
                    int incorrectFilesIndex = 0;
                    // Checks to make sure that all files are of type PNG or JPG
                    for (int i = 0; i < files.length; i++) {
                        filePath = files[i].toString();
                        fileName = filePath.substring(filePath.lastIndexOf('\\')+1);
                        if (!fileName.contains(".png") && !fileName.contains(".jpg")){
                            // Makes list of incorrect files
                            incorrectFiles[incorrectFilesIndex++] = i;
                            incorrectFileTypesFound = true;
                            continue;
                        }
                        fileCount++;
                    }
                    // Removes unwanted files
                    removeValues(incorrectFiles);

                    selectedFile.setText(String.format("%d Files Selected", fileCount));
                    if (incorrectFileTypesFound) {
                        setErrorText("Some files were not of the PNG or JPG format", true);
                    } else {
                        // This is incase there is a reselection of files.
                        hideErrorText();
                    }

                }
               
            }

        }

        // This section will Resize the images correctly and also save them to the selected folder
        if (e.getSource() == formatButton) {
            
            // Checks if there are any selected files
            if (files == null || files.length == 0) {
                setErrorText("You need to at least one file first", true);
            } else {

                // Select folder destination for new files
                JFileChooser folderChooser = new JFileChooser();
                folderChooser.setCurrentDirectory(new File("."));
                folderChooser.setDialogTitle("Choose File Location");
                folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                folderChooser.setAcceptAllFileFilterUsed(false);

                int response = folderChooser.showOpenDialog(null);

                if (response == JFileChooser.APPROVE_OPTION) {
                    String folderPath = folderChooser.getSelectedFile().toString();

                    selectedFile.setText("File Successfully Formatted!");
                    hideErrorText();

                    // Read through each file, write the new file, and save it
                    for ( int i = 0; i < files.length; i++) {
                        try {

                            File currentFile = files[i];

                            // Creating new file
                            int pngPos = currentFile.toString().indexOf(".png");
                            int filePos = currentFile.toString().lastIndexOf('\\');
                            String newFileName = folderPath + currentFile.toString().substring(filePos, pngPos) + "-formatted.png";
                            File newFile = new File(newFileName);
                            int hPos = 0;
                            int wPos = 0;
                            int newFileHeight = 64; // pixels
                            int newFileWidth = 128; // pixels
                            BufferedImage newImage = new BufferedImage(newFileWidth,newFileHeight,BufferedImage.TYPE_INT_ARGB);
                            Graphics2D g2d = newImage.createGraphics();

                            g2d.setBackground(new Color(Color.TRANSLUCENT));

                            // Getting current image into a useable state
                            Raster currentImage = ImageIO.read(currentFile).getData();
                            int height = currentImage.getHeight();
                            int width = currentImage.getWidth();

                            // Looping through each of the current Images pixels by row
                            for (int y = 0; y < height; y++) {
                                for (int x = 0; x < width; x++) {
                                    int[] pixel = currentImage.getPixel(x,y,new int[4]);
                                    int r = pixel[0];
                                    int g = pixel[1];
                                    int b = pixel[2];
                                    int a = pixel[3];

                                    Color c = new Color(r,g,b,a);
                                    // Background colors we want to exclude
                                    Color exclude = new Color(96,152,128);
                                    Color exclude2 = new Color(99,157,133);
                                    Color exclude3 = new Color(96,153,129);
                                    Color exclude4 = new Color(107, 175, 157);

                                    // Write to new file with the color
                                    if (!c.equals(exclude) && !c.equals(exclude2) && !c.equals(exclude3) && !c.equals(exclude4) && a != 0) {
                                        g2d.setColor(c);
                                        g2d.fillRect(wPos, hPos, 2, 2);
                                    }
                                    wPos += 2;
                                }
                                hPos += 2;
                                if (y == (height/2)-1) {
                                    hPos = 0;
                                    wPos = height;
                                } else if (y > (height/2)-1) {
                                    wPos = height;
                                } else {
                                    wPos = 0;
                                }
                            }
                            
                            // Writes and Saves image
                            ImageIO.write(newImage, "png", newFile);

                        } catch (FileNotFoundException fe) {
                            setErrorText("Error has occurred with file!", true);
                            System.out.println("An error occurred.");
                            fe.printStackTrace();
                        } catch (IOException e1) {
                            setErrorText("Error has occurred with file!", true);
                            System.out.println("An error occurred.");
                            e1.printStackTrace();
                        }
                    }

                }

            }
        }
    }

    private void removeValues(int[] indexes) {
        File[] temp = Arrays.copyOf(files, files.length);
        for (int i = 0; i < indexes.length; i++) {
            int excludeFile = indexes[i];
            for (int j = excludeFile; j < temp.length-(i+1); j++) {
                temp[j] = temp[j+1];
            }
        }
        files = Arrays.copyOf(temp, temp.length-indexes.length);
    }

    private void setErrorText(String msg, Boolean visible) {
        String html = "<html><div style=\"width:%dpx; padding:0px 20px 0px 30px;\">%s</div></html>";
        int stringWidth = 150;
        errorText.setText(String.format(html, stringWidth, msg));
        errorText.setVisible(visible);
    }

    private void hideErrorText() {
        setErrorText("", false);
    }
}
