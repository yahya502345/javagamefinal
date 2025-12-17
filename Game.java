import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

public class Game extends Frame {

    // UI Components of this game for better
    private GraphicButton startBtn, pauseBtn, quitBtn;
    private List<GraphicButton> buttons = new ArrayList<>();

    // Backgrounds image of start,pause,quit
    private Image backgroundImage; // Menu BG
    private Image[] gameBgImages = new Image[4]; // Animated Game BGs
    private int currentBgFrame = 0;

    // Game Objects
    private Player player;
    private Samurai samurai;

    // Audio/Thread
    private Clip audioClip;
    private Thread gameLoopThread;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    // Game State
    private boolean isPlaying = false;

    public Game() {
        setTitle("Java Game - OOP Structure");
        setSize(WIDTH, HEIGHT);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                isPlaying = false;
                System.exit(0);
            }
        });

        // Initialize Player and Samurai
        player = new Player(350, 250);
        samurai = new Samurai(400, 250);

        // Initialize UI Layout
        initializeLayout();

        // Mouse Listeners
        MyMouseListener mouseListener = new MyMouseListener();
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);

        // Key Listener for WASD
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isPlaying) {
                    player.setInput(e.getKeyCode(), true);
                    samurai.setInput(e.getKeyCode(), true);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (isPlaying) {
                    player.setInput(e.getKeyCode(), false);
                    samurai.setInput(e.getKeyCode(), false);
                }
            }
        });

        loadResources();
        setVisible(true);
    }

    private void initializeLayout() {
        int w = 200;
        int h = 100;
        int spacing = 30;
        int startX = (WIDTH - (3 * w + 2 * spacing)) / 2;
        int startY = HEIGHT - 120;

        startBtn = new GraphicButton("Start", startX, startY, w, h);
        pauseBtn = new GraphicButton("Pause", startX + w + spacing, startY, w, h);
        quitBtn = new GraphicButton("Quit", startX + 2 * (w + spacing), startY, w, h);

        buttons.add(startBtn);
        buttons.add(pauseBtn);
        buttons.add(quitBtn);
    }

    private void loadResources() {
        // Menu Background
        loadImage("background", (img) -> backgroundImage = img);

        // Game Animation Backgrounds
        for (int i = 0; i < 4; i++) {
            final int index = i;
            loadImage("bg" + (i + 1), (img) -> gameBgImages[index] = img);
        }
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        // Buttons
        loadImage("start_btn", (img) -> startBtn.setImage(img));
        loadImage("pause_btn", (img) -> pauseBtn.setImage(img));
        loadImage("quit_btn", (img) -> quitBtn.setImage(img));

        // Audio
        try {
            File audioFile = new File("assets/music.wav");
            if (audioFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                audioClip = AudioSystem.getClip();
                audioClip.open(audioStream);
                audioClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.out.println("Error loading audio: " + e.getMessage());
        }
    }

    private interface ImageSetter {
        void set(Image img);
    }

    private void loadImage(String baseName, ImageSetter setter) {
        String[] extensions = { ".png", ".jpg", ".jpeg" };
        boolean found = false;

        for (String ext : extensions) {
            File f = new File("assets/" + baseName + ext);
            if (f.exists()) {
                try {
                    setter.set(ImageIO.read(f));
                    found = true;
                    break;
                } catch (IOException e) {
                    System.out.println("Error reading " + f.getName());
                }
            }
        }
    }

    private class MyMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (!isPlaying) {
                handleClick(e.getX(), e.getY());
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (!isPlaying) {
                handleHover(e.getX(), e.getY());
            }
        }
    }

    private void handleClick(int x, int y) {
        if (startBtn.contains(x, y)) {
            System.out.println("Start Game clicked");
            isPlaying = true;
            startAnimationLoop();

            this.requestFocus();
            this.requestFocusInWindow();
        } else if (pauseBtn.contains(x, y)) {
            System.out.println("Pause Game clicked");
        } else if (quitBtn.contains(x, y)) {
            System.exit(0);
        }
    }

    private void startAnimationLoop() {
        gameLoopThread = new Thread(() -> {
            while (isPlaying) {
                // Game Logic Step
                player.physicsUpdate();
                player.update(); // Animation
                samurai.physicsUpdate();
                samurai.update(); // Manual Control

                checkCombat();

                currentBgFrame = (currentBgFrame + 1) % 4;
                repaint();
                try {
                    Thread.sleep(40); // ~30 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        gameLoopThread.start();
    }

    private void checkCombat() {
        // Range check (using center points)
        Rectangle pBounds = player.getBounds();
        Rectangle sBounds = samurai.getBounds();

        double pCx = pBounds.getCenterX();
        double pCy = pBounds.getCenterY();
        double sCx = sBounds.getCenterX();
        double sCy = sBounds.getCenterY();

        double dist = Math.sqrt(Math.pow(pCx - sCx, 2) + Math.pow(pCy - sCy, 2));
        int attackRange = 50; // Pixels
        int damage = 5;

        // Player attacking Samurai
        if (player.isAttackingPublic() && !player.hasDealtDamage()) {
            if (dist < attackRange) {
                if (!samurai.isShieldingPublic()) {
                    samurai.takeDamage(damage);
                } else {
                    //it take damage
                }
                player.setDamageDealt(true); // One hit per swing
            }
        }

        // Samurai attacking Player
        if (samurai.isAttackingPublic() && !samurai.hasDealtDamage()) {
            if (dist < attackRange) {
                if (!player.isShieldingPublic()) {
                    player.takeDamage(damage);
                }
                samurai.setDamageDealt(true);
            }
        }

    }

    // Double Buffering
    private Image offScreenImage;
    private Graphics offScreenGraphics;

    private void drawHealthBars(Graphics2D g2) {
        int barWidth = 300;
        int barHeight = 25;
        int margin = 30; // Increased margin

        // Font setup
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();

        // ----------------- PLAYER 1 (Left) -----------------
        // Background for Bar
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(margin - 4, margin - 4, barWidth + 8, barHeight + 8);

        // Health Bar
        g2.setColor(Color.RED.darker());
        g2.fillRect(margin, margin, barWidth, barHeight);
        g2.setColor(Color.GREEN);
        int pWidth = (int) ((player.getHealth() / (double) player.getMaxHealth()) * barWidth);
        g2.fillRect(margin, margin, pWidth, barHeight);

        // Border
        g2.setColor(Color.WHITE);
        g2.drawRect(margin, margin, barWidth, barHeight);

        // Text (Shadowed)
        String p1Text = "PLAYER 1";
        int textY = margin + 19;
        g2.setColor(Color.BLACK);
        g2.drawString(p1Text, margin + 12, textY + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(p1Text, margin + 10, textY);

        // ----------------- PLAYER 2 (Right) -----------------
        int x2 = WIDTH - barWidth - margin;

        // Background for Bar
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(x2 - 4, margin - 4, barWidth + 8, barHeight + 8);

        // Health Bar
        g2.setColor(Color.RED.darker());
        g2.fillRect(x2, margin, barWidth, barHeight);
        g2.setColor(Color.GREEN);
        int sWidth = (int) ((samurai.getHealth() / (double) samurai.getMaxHealth()) * barWidth);
        g2.fillRect(x2, margin, sWidth, barHeight);

        // Border
        g2.setColor(Color.WHITE);
        g2.drawRect(x2, margin, barWidth, barHeight);

        // Shadow
        String p2Text = "PLAYER 2";
        int p2TextWidth = fm.stringWidth(p2Text);
        g2.setColor(Color.BLACK);
        g2.drawString(p2Text, x2 + barWidth - p2TextWidth - 8, textY + 1);
        g2.setColor(Color.WHITE);
        g2.drawString(p2Text, x2 + barWidth - p2TextWidth - 10, textY);

        //  WIN
        if (player.isDead() || samurai.isDead()) {
            String msg = player.isDead() ? "SAMURAI WINS!" : "FIGHTER WINS!";

            g2.setFont(new Font("Arial", Font.BOLD, 60));
            fm = g2.getFontMetrics();
            int msgWidth = fm.stringWidth(msg);
            int msgX = (WIDTH - msgWidth) / 2;
            int msgY = HEIGHT / 2;

            // Dark Background Box for Text
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRoundRect(msgX - 40, msgY - 60, msgWidth + 80, 90, 20, 20);

            // Text Shadow
            g2.setColor(Color.BLACK);
            g2.drawString(msg, msgX + 4, msgY + 4);

            // Main Text
            g2.setColor(Color.YELLOW);
            g2.drawString(msg, msgX, msgY);

            g2.setFont(new Font("Arial", Font.BOLD, 20));
            String subMsg = "Close window to quit";
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString(subMsg, (WIDTH - g2.getFontMetrics().stringWidth(subMsg)) / 2, msgY + 50);
        }
    }

    private void handleHover(int x, int y) {
        boolean repaintNeeded = false;
        for (GraphicButton btn : buttons) {
            boolean wasHovered = btn.isHovered();
            boolean isNowHovered = btn.contains(x, y);
            btn.setHovered(isNowHovered);

            if (isNowHovered != wasHovered) {
                repaintNeeded = true;
            }
        }
        if (repaintNeeded) {
            repaint();
        }
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        // Initialize Double Buffer
        if (offScreenImage == null) {
            offScreenImage = createImage(WIDTH, HEIGHT);
            if (offScreenImage == null) {
                repaint();
                return;
            }
            offScreenGraphics = offScreenImage.getGraphics();
        }

        // Draw to Offscreen Buffer
        Graphics2D g2d = (Graphics2D) offScreenGraphics;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 1. Draw Background
        if (isPlaying) {
            Image currentBg = gameBgImages[currentBgFrame];
            if (currentBg != null) {
                g2d.drawImage(currentBg, 0, 0, WIDTH, HEIGHT, this);
            } else if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, this);
            } else {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, WIDTH, HEIGHT);
            }
        } else {
            // Menu BG
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, this);
            } else {
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRect(0, 0, WIDTH, HEIGHT);
            }
        }

        // 2. Draw Game Objects
        if (isPlaying) {
            player.draw(g2d);
            samurai.draw(g2d);
            drawHealthBars(g2d);
        } else {
            // Draw Menu Buttons
            for (GraphicButton btn : buttons) {
                btn.draw(g2d);
            }
        }

        // 3. Draw Buffer to Screen
        g.drawImage(offScreenImage, 0, 0, this);
    }
}
