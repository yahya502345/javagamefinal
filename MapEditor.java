import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class MapEditor extends Frame {

    private BufferedImage bgImage;
    private BufferedImage maskImage;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    // Brush size
    private int brushSize = 100;

    public MapEditor() {
        setTitle("Map Editor - Paint Walkable Areas (White)");
        setSize(WIDTH, HEIGHT);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);

        // Load BG
        try {
            File f = new File("assets/bg1.png");
            if (f.exists()) {
                Image raw = ImageIO.read(f);
                // Resize to 800x600 strictly
                bgImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
                Graphics g = bgImage.getGraphics();
                g.drawImage(raw, 0, 0, WIDTH, HEIGHT, null);
                g.dispose();
            } else {
                System.out.println("assets/bg1.png not found!");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create Mask (Start Black = Blocked)
        maskImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = maskImage.createGraphics();
        g2.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black overlay initially
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.dispose();

        // Listeners for painting
        MyMouseInput input = new MyMouseInput();
        addMouseListener(input);
        addMouseMotionListener(input);

        // Save on Close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                saveMapAndExit();
            }
        });

        setVisible(true);
    }

    private void paintAt(int x, int y) {
        Graphics2D g2 = maskImage.createGraphics();
        // Clear the black "fog" to reveal "White" (Transparent/Walkable) or actually
        // paint White?
        // User said "turns white".
        // Let's assume:
        // Final Output 0 = Blocked, 1 = Walkable.
        // Visually: Dark = Blocked. Light/White = Walkable.

        g2.setColor(Color.WHITE);
        g2.fillOval(x - brushSize / 2, y - brushSize / 2, brushSize, brushSize);
        g2.dispose();
        repaint();
    }

    private void saveMapAndExit() {
        System.out.println("Generating Collision Map...");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("assets/collision.txt"));

            // Write Dimensions first (Optional, but good for parsing)
            // Or just raw data. Let's do raw lines.

            for (int y = 0; y < HEIGHT; y++) {
                StringBuilder line = new StringBuilder();
                for (int x = 0; x < WIDTH; x++) {
                    int rgb = maskImage.getRGB(x, y);
                    // Check if pixel is White (Walkable)
                    // We painted WHITE The background might be different.
                    // Strict check or brightness check.
                    Color c = new Color(rgb, true);

                    // If we painted White, R=255, G=255, B=255.
                    // If we initialized with transparent black, those pixels are dark.
                    if (c.getRed() > 200 && c.getGreen() > 200 && c.getBlue() > 200) {
                        line.append("1");
                    } else {
                        line.append("0");
                    }


                }
                writer.write(line.toString());
                writer.newLine(); // New line for next row
            }

            writer.close();
            System.out.println("Saved to assets/collision.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    @Override
    public void paint(Graphics g) {
        // Draw BG
        g.drawImage(bgImage, 0, 0, null);
        // Draw Mask Overlay
        // We want blocked areas (unpainted) to look dark/blocked.
        // Walkable areas (painted white) to be clear or white.

        // Since maskImage started as 150-alpha Black, and we paint Solid White on it:
        // Dark areas are the original overlay.
        // White spots are where we clicked.

        // Use a composite to blend properly if needed, but simple draw works for visual
        // feedback.
        g.drawImage(maskImage, 0, 0, null);
    }

    class MyMouseInput extends MouseAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            // Need to adjust for Insets (Title bar) as Frame includes them
            Insets insets = getInsets();
            int x = e.getX() - insets.left;
            int y = e.getY() - insets.top;
            paintAt(x, y);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            Insets insets = getInsets();
            int x = e.getX() - insets.left;
            int y = e.getY() - insets.top;
            paintAt(x, y);
        }
    }

    public static void main(String[] args) {
        new MapEditor();
    }
}
