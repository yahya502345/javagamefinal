import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Player {
    private int x, y;
    private int width = 128, height = 128; // Scaled to sprite size
    private int speed = 8; // tweaked speed

    // Combat State
    private int maxHealth = 100;
    private int currentHealth = 100;
    private boolean isDead = false;
    private boolean damageDealt = false; // To prevent multi-hit per swing

    // Animation Frames
    private BufferedImage[] idleLeft, idleRight;
    private BufferedImage[] walkLeft, walkRight;
    private BufferedImage[] attack1Left, attack1Right;
    private BufferedImage[] attack2Left, attack2Right;
    private BufferedImage[] shieldLeft, shieldRight;
    private BufferedImage[] deadLeft, deadRight;

    // Current Animation State
    private BufferedImage[] currentFrames;
    private int currentFrameIndex = 0;
    private int animationTick = 0;
    private int animationSpeed = 4;

    // State
    private boolean isMoving = false;
    private boolean isAttacking = false;
    private boolean isShielding = false;
    private boolean facingLeft = false;

    public boolean isAttackingPublic() {
        return isAttacking;
    }

    public boolean isShieldingPublic() {
        return isShielding;
    }

    public boolean isDead() {
        return isDead;
    }

    public int getHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean hasDealtDamage() {
        return damageDealt;
    }

    public void setDamageDealt(boolean dealt) {
        this.damageDealt = dealt;
    }

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        loadResources();
        currentFrames = idleRight;
    }

    private void loadResources() {
        idleLeft = loadSpriteSheet("assets/characters/Fighter/A/Idle.png");
        walkLeft = loadSpriteSheet("assets/characters/Fighter/A/Walk.png");
        attack1Left = loadSpriteSheet("assets/characters/Fighter/A/Attack_1.png");
        attack2Left = loadSpriteSheet("assets/characters/Fighter/A/Attack_2.png");
        shieldLeft = loadSpriteSheet("assets/characters/Fighter/A/Shield.png");
        deadLeft = loadSpriteSheet("assets/characters/Fighter/A/Dead.png");

        idleRight = loadSpriteSheet("assets/characters/Fighter/D/Idle.png");
        walkRight = loadSpriteSheet("assets/characters/Fighter/D/Walk.png");
        attack1Right = loadSpriteSheet("assets/characters/Fighter/D/Attack_1.png");
        attack2Right = loadSpriteSheet("assets/characters/Fighter/D/Attack_2.png");
        shieldRight = loadSpriteSheet("assets/characters/Fighter/D/Shield.png");
        deadRight = loadSpriteSheet("assets/characters/Fighter/D/Dead.png");

        if (idleRight == null)
            System.out.println("Error: Fighter idleRight is null");
        if (deadRight == null)
            System.out.println("Error: Fighter deadRight is null");
    }

    private BufferedImage[] loadSpriteSheet(String path) {
        try {
            File f = new File(path);
            if (!f.exists())
                return null;
            BufferedImage sheet = ImageIO.read(f);
            int h = sheet.getHeight();
            int w = sheet.getWidth();
            int count = w / h;
            BufferedImage[] frames = new BufferedImage[count];
            for (int i = 0; i < count; i++) {
                frames[i] = sheet.getSubimage(i * h, 0, h, h);
            }
            return frames;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void takeDamage(int amount) {
        if (isDead || isShielding)
            return; // Basic shield blocks all for now

        currentHealth -= amount;
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
            currentFrameIndex = 0;
            currentFrames = facingLeft ? deadLeft : deadRight;
        }
    }

    public void update() {
        // Animation Frame
        animationTick++;
        if (animationTick >= animationSpeed) {
            animationTick = 0;
            currentFrameIndex++;

            if (currentFrames != null && currentFrameIndex >= currentFrames.length) {
                if (isDead) {
                    currentFrameIndex = currentFrames.length - 1; // Stay dead
                } else if (isAttacking) {
                    isAttacking = false;
                    damageDealt = false; // Reset for next attack
                    currentFrameIndex = 0;
                    resetAnimationState();
                } else {
                    currentFrameIndex = 0;
                }
            }
        }
    }

    private void resetAnimationState() {
        if (isDead)
            return;
        if (isMoving) {
            currentFrames = facingLeft ? walkLeft : walkRight;
        } else {
            currentFrames = facingLeft ? idleLeft : idleRight;
        }
    }

    public void setInput(int keyCode, boolean pressed) {
        if (isDead)
            return;

        if (keyCode == KeyEvent.VK_A) {
            if (pressed) {
                isMoving = true;
                facingLeft = true;
                if (!isAttacking && !isShielding)
                    currentFrames = walkLeft;
            } else if (facingLeft) {
                isMoving = false;
                if (!isAttacking && !isShielding)
                    currentFrames = idleLeft;
            }
        }
        if (keyCode == KeyEvent.VK_D) {
            if (pressed) {
                isMoving = true;
                facingLeft = false;
                if (!isAttacking && !isShielding)
                    currentFrames = walkRight;
            } else if (!facingLeft) {
                isMoving = false;
                if (!isAttacking && !isShielding)
                    currentFrames = idleRight;
            }
        }

        if (pressed && !isAttacking && !isShielding) {
            if (keyCode == KeyEvent.VK_J) {
                isAttacking = true;
                damageDealt = false;
                currentFrameIndex = 0;
                currentFrames = facingLeft ? attack1Left : attack1Right;
            } else if (keyCode == KeyEvent.VK_K) {
                isAttacking = true;
                damageDealt = false;
                currentFrameIndex = 0;
                currentFrames = facingLeft ? attack2Left : attack2Right;
            } else if (keyCode == KeyEvent.VK_L) {
                isShielding = true;
                currentFrameIndex = 0;
                currentFrames = facingLeft ? shieldLeft : shieldRight;
            }
        } else if (!pressed) {
            if (keyCode == KeyEvent.VK_L) {
                isShielding = false;
                resetAnimationState();
            }
        }
    }

    public void physicsUpdate() {
        if (isDead || isAttacking || isShielding)
            return;

        if (isMoving) {
            int nextX = x + (facingLeft ? -speed : speed);
            int nextY = y;
            if (canMoveTo(nextX, nextY)) {
                x = nextX;
                y = nextY;
            }
        }
    }

    // ... (rest of collision logic kept same, verify collisionData usage)
    private boolean canMoveTo(int nextX, int nextY) {
        int hitboxSize = 64;
        int offsetX = (width - hitboxSize) / 2;
        int offsetY = (height - hitboxSize) / 2;
        int hx = nextX + offsetX;
        int hy = nextY + offsetY;
        return isWalkable(hx, hy) && isWalkable(hx + hitboxSize, hy) &&
                isWalkable(hx, hy + hitboxSize) && isWalkable(hx + hitboxSize, hy + hitboxSize);
    }

    private boolean isWalkable(int px, int py) {
        if (px < 0 || px >= 800 || py < 0 || py >= 600)
            return false;
        try {
            return CollisionData.map[py][px] == 1;
        } catch (Exception e) {
            return false;
        }
    }

    public void draw(Graphics2D g2) {
        if (currentFrames != null && currentFrameIndex < currentFrames.length) {
            g2.drawImage(currentFrames[currentFrameIndex], x, y, width, height, null);
        } else {
            g2.setColor(Color.RED);
            g2.fillRect(x, y, width, height);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
