import java.io.*;

public class CollisionData {
    // 800x600 Resolution
    // 0 = Blocked
    // 1 = Walkable
    public static int[][] map = new int[600][800];

    static {
        loadMap();
    }

    public static void loadMap() {
        System.out.println("Loading collision map...");
        File f = new File("assets/collision.txt");
        if (!f.exists()) {
            System.out.println("No collision map found (assets/collision.txt). Defaulting to Walkable.");
            // Default to 1
            for (int r = 0; r < 600; r++) {
                for (int c = 0; c < 800; c++)
                    map[r][c] = 1;
            }
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            for (int row = 0; row < 600; row++) {
                String line = br.readLine();
                if (line == null)
                    break;
                for (int col = 0; col < 800 && col < line.length(); col++) {
                    char c = line.charAt(col);
                    map[row][col] = (c == '1') ? 1 : 0;
                }
            }
            System.out.println("Collision map loaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
