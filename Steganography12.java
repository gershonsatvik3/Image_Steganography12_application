import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

class MultipleButtonsEvent implements ActionListener {
    private static int bytesForTextLengthData = 4;
    private static int bitsInByte = 8;
    private static String s = new String();
    private static String s1 = new String();
    JButton btn1, btn2;
    JFrame frame;
    JTextField tf1, tf2, tf3;

    public MultipleButtonsEvent() {
        frame = new JFrame("Image Steganography");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(250, 130, 450, 400);
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(null);

        JTextField tf = new JTextField("Steganography");
        tf.setBounds(150, 15, 160, 40);
        tf.setEditable(false);
        tf.setFont(new Font("Aleo", Font.BOLD, 22));
        tf.setBorder(null);
        contentPane.add(tf);

        JTextField tf4 = new JTextField("Enter Image Path :");
        tf4.setBounds(10, 90, 120, 30);
        tf4.setEditable(false);
        tf4.setBorder(null);
        contentPane.add(tf4);

        tf1 = new JTextField();
        tf1.setEditable(true);
        tf1.setBounds(150, 90, 270, 30);

        contentPane.add(tf1);

        JTextField tf5 = new JTextField("Enter Text Path :");
        tf5.setBounds(10, 130, 120, 30);
        tf5.setEditable(false);
        tf5.setBorder(null);
        contentPane.add(tf5);

        tf2 = new JTextField();
        tf2.setEditable(true);
        tf2.setBounds(150, 130, 270, 30);
        contentPane.add(tf2);

        btn1 = new JButton("Encode");
        btn1.setBounds(170, 180, 100, 30);
        contentPane.add(btn1);
        btn1.addActionListener(this);

        JTextField tf6 = new JTextField("Enter Image Path :");
        tf6.setBounds(10, 250, 120, 30);
        tf6.setEditable(false);
        tf6.setBorder(null);
        contentPane.add(tf6);

        tf3 = new JTextField();
        tf3.setEditable(true);
        tf3.setBounds(150, 250, 270, 30);
        contentPane.add(tf3);

        btn2 = new JButton("Decode");
        btn2.setBounds(170, 300, 100, 30);
        contentPane.add(btn2);
        btn2.addActionListener(this);

        frame.setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

        if (e.getSource() == btn1) {
            String text1 = tf1.getText();
            String text2 = tf2.getText();
            encode(text1, text2);
            JOptionPane.showMessageDialog(frame, s);

        }
        if (e.getSource() == btn2) {
            String text3 = tf3.getText();
            decode(text3);
            JOptionPane.showMessageDialog(frame, s);
            JOptionPane.showMessageDialog(frame, s1);
        }

    }

    private static String decode(String imagePath) {
        byte[] decodedHiddenText;
        try {
            BufferedImage imageFromPath = getImageFromPath(imagePath);
            BufferedImage imageInUserSpace = getImageInUserSpace(imageFromPath);
            byte imageInBytes[] = getBytesFromImage(imageInUserSpace);
            decodedHiddenText = decodeImage(imageInBytes);
            String hiddenText = new String(decodedHiddenText);
            String outputFileName = "hidden_text.txt";
            s1 = hiddenText;
            saveTextToPath(hiddenText, new File(outputFileName));
            System.out.println("\n\nSuccessfully extracted text to: " + outputFileName);
            s = "Successfully extracted text to: " + outputFileName;
            return hiddenText;
        } catch (Exception exception) {
            System.out.println("No hidden message. Error: " + exception);
            s = "No hidden message. Error: " + exception;
            return "";
        }
    }

    private static byte[] decodeImage(byte[] image) {
        int length = 0;
        int offset = bytesForTextLengthData * bitsInByte;

        for (int i = 0; i < offset; i++) {
            length = (length << 1) | (image[i] & 0x1);
        }

        byte[] result = new byte[length];

        for (int b = 0; b < result.length; b++) {
            for (int i = 0; i < bitsInByte; i++, offset++) {
                result[b] = (byte) ((result[b] << 1) | (image[offset] & 0x1));
            }
        }
        return result;
    }

    private static void encode(String imagePath, String textPath) {
        BufferedImage originalImage = getImageFromPath(imagePath);
        BufferedImage imageInUserSpace = getImageInUserSpace(originalImage);
        String text = getTextFromTextFile(textPath);

        byte imageInBytes[] = getBytesFromImage(imageInUserSpace);
        byte textInBytes[] = text.getBytes();
        byte textLengthInBytes[] = getBytesFromInt(textInBytes.length);
        try {
            encodeImage(imageInBytes, textLengthInBytes, 0);
            encodeImage(imageInBytes, textInBytes, bytesForTextLengthData * bitsInByte);
        } catch (Exception exception) {
            System.out.println("Couldn't hide text in image. Error: " + exception);
            s = "Couldn't hide text in image. Error: " + exception;
            return;
        }

        String fileName = imagePath;
        int position = fileName.lastIndexOf(".");
        if (position > 0) {
            fileName = fileName.substring(0, position);
        }

        String finalFileName = fileName + "_with_hidden_message.png";
        System.out.println("\n\nSuccessfully encoded text in: " + finalFileName);
        s = "Successfully encoded text in: " + finalFileName;
        saveImageToPath(imageInUserSpace, new File(finalFileName), "png");
        return;
    }

    private static byte[] encodeImage(byte[] image, byte[] addition, int offset) {
        if (addition.length + offset > image.length) {
            throw new IllegalArgumentException("Image file is not long enough to store provided text");
        }
        for (int i = 0; i < addition.length; i++) {
            int additionByte = addition[i];
            for (int bit = bitsInByte - 1; bit >= 0; --bit, offset++) {
                int b = (additionByte >>> bit) & 0x1;
                image[offset] = (byte) ((image[offset] & 0xFE) | b);
            }
        }
        return image;
    }

    private static void saveImageToPath(BufferedImage image, File file, String extension) {
        try {
            file.delete();
            ImageIO.write(image, extension, file);
        } catch (Exception exception) {
            System.out.println("Image file could not be saved. Error: " + exception);
            s = "Image file could not be saved. Error: " + exception;
        }
    }

    private static void saveTextToPath(String text, File file) {
        try {
            if (file.exists() == false) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(text);
            bufferedWriter.close();
        } catch (Exception exception) {
            System.out.println("Couldn't write text to file: " + exception);
            s = "Couldn't write text to file: " + exception;
        }
    }

    private static BufferedImage getImageFromPath(String path) {
        BufferedImage image = null;
        File file = new File(path);
        try {
            image = ImageIO.read(file);
        } catch (Exception exception) {
            System.out.println("Input image cannot be read. Error: " + exception);
            s = "Couldn't write text to file: " + exception;
        }
        return image;
    }

    private static String getTextFromTextFile(String textFile) {
        String text = "";
        try {
            Scanner scanner = new Scanner(new File(textFile));
            text = scanner.useDelimiter("\\A").next();
            scanner.close();
        } catch (Exception exception) {
            System.out.println("Couldn't read text from file. Error: " + exception);
            s = "Couldn't read text from file. Error: " + exception;
        }
        return text;
    }

    // Helpers

    private static BufferedImage getImageInUserSpace(BufferedImage image) {
        BufferedImage imageInUserSpace = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = imageInUserSpace.createGraphics();
        graphics.drawRenderedImage(image, null);
        graphics.dispose();
        return imageInUserSpace;
    }

    private static byte[] getBytesFromImage(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
        return buffer.getData();
    }

    private static byte[] getBytesFromInt(int integer) {
        return ByteBuffer.allocate(bytesForTextLengthData).putInt(integer).array();
    }
}

public class Steganography12 {
    public static void main(String[] args) {
        MultipleButtonsEvent btnevent = new MultipleButtonsEvent();

    }

}