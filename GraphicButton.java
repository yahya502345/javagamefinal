import java.awt.*;

public class GraphicButton {
    private Image image;
    private Rectangle bounds;
    private String name;
    private boolean isHovered = false;

    public GraphicButton(String name, int x, int y, int w, int h) {
        this.name = name;
        this.bounds = new Rectangle(x, y, w, h);
    }

    public void setImage(Image img) {
        this.image = img;
    }

    public boolean contains(int x, int y) {
        return bounds.contains(x, y);
    }

    public void setHovered(boolean hovered) {
        this.isHovered = hovered;
    }

    public boolean isHovered() {
        return isHovered;
    }

    public void draw(Graphics2D g2) {
        if (image != null) {
            g2.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, null);
        } else {
            g2.setColor(Color.LIGHT_GRAY);
            g2.fill(bounds);
            g2.setColor(Color.BLACK);
            g2.draw(bounds);
            g2.drawString(name, bounds.x + 10, bounds.y + 30);
        }
    }
}
