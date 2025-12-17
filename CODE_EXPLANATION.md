# Project Code Documentation

This document provides a detailed explanation of each file, the class it contains, and a breakdown of every function within that class.

## Table of Contents
1. [Main.java](#mainjava)
2. [Game.java](#gamejava)
3. [Player.java](#playerjava)
4. [Samurai.java](#samuraijava)
5. [CollisionData.java](#collisiondatajava)
6. [GraphicButton.java](#graphicbuttonjava)
7. [MapEditor.java](#mapeditorjava)

---

## Main.java
**Class Description**: The entry point of the application.

**Functions**:
- `main(String[] args)`: The standard Java entry point. It simply creates a new instance of `Game()`, launching the application window.

---

## Game.java
**Class Description**: The central engine of the game. It extends `java.awt.Frame` to create the window and manages the game loop, rendering, input handling, and game state (Menu vs Playing).

**Functions**:
- `Game()`: The constructor. Sets up the window properties (size, title, layout), initializes game objects (`Player`, `Samurai`), sets up UI buttons, attaches event listeners (Mouse, Keyboard, Window), loads assets, and makes the window visible.
- `initializeLayout()`: Calculates the geometric positions for the "Start", "Pause", and "Quit" buttons to center them at the bottom of the screen.
- `loadResources()`: Loads all necessary assets: background images for the menu and game loop, button images, and background music.
- `loadImage(String baseName, ImageSetter setter)`: A helper function that tries to load an image with multiple extensions (`.png`, `.jpg`, `.jpeg`) to avoid "File Not Found" errors if the extension varies.
- `handleClick(int x, int y)`: Handles mouse clicks when in the Menu state. Checks if the click matches the bounds of Start (begins game), Pause (no-op currently), or Quit (exits app) buttons.
- `startAnimationLoop()`: Creates and starts a new Thread that runs the game loop. The loop updates game logic (physics, animation, combat) and repaints the screen ~25 times per second.
- `checkCombat()`: Logic to handle interactions between Player and Samurai. Calculates distance, checks if an attack key is pressed, and applies damage if within range and not shielded. Matches hits to prevent infinite damage per single swing.
- `drawHealthBars(Graphics2D g2)`: Renders the Heads-Up Display (HUD). Draws health bars for Player 1 and Player 2, their names with shadow effects, and the "Variables" (health %) visualization. Also draws the "WINNER" overlay if a player dies.
- `handleHover(int x, int y)`: Checks if the mouse acts over any button in the menu to toggle their "hover" visual state.
- `update(Graphics g)`: Directed by the system to update the container. Overridden to call `paint` directly, preventing default clearing which causes flickering.
- `paint(Graphics g)`: Handles the "Double Buffering" rendering strategy. It draws everything to an off-screen image first (Background -> Characters -> UI), then draws the final image to the screen.

**Inner Classes**:
- `MyMouseListener`: Extends `MouseAdapter`. Captures `mousePressed` and `mouseMoved` events and delegates them to `handleClick` or `handleHover` only if the game is NOT currently playing.

---

## Player.java
**Class Description**: Represents the user-controlled Fighter character. Handles loading specific fighter assets, processing WASD input, and managing internal state (Health, Animation Frame, Position).

**Functions**:
- `Player(int startX, int startY)`: Constructor. Sets initial position and loads sprite resources.
- `loadResources()`: Loads all sprite sheets (Idle, Walk, Attack, Shield, Dead) for both facing directions (Left/Right).
- `loadSpriteSheet(String path)`: Splits a single strip image into an array of individual `BufferedImage` frames.
- `takeDamage(int amount)`: Reduces health by the specified amount. Logic prevents damage if shielding or already dead. Triggers "Dead" animation if health hits 0.
- `update()`: Advances the animation state (ticks). Handles looping for idle/walk, and one-shot playback for attacks. Resets to idle after an attack finishes.
- `setInput(int keyCode, boolean pressed)`: Handles Keyboard input. Maps `A/D` to movement flags and `J/K/L` to Attack/Shield actions. Has logic to prevent moving while attacking or shielding.
- `physicsUpdate()`: Calculates potential new position based on speed and direction. Calls `canMoveTo` to verify collision before applying the move.
- `canMoveTo(int nextX, int nextY)`: Checks the 4 corners of the character's hitbox against the global collision map.
- `isWalkable(int px, int py)`: Helper that looks up a specific pixel coordinate in `CollisionData.map`.
- `draw(Graphics2D g2)`: Renders the current animation frame at the character's (x, y) coordinates.
- `getBounds()`: Returns a `Rectangle` representing the character's hit area.
- Getters (`isAttackingPublic`, `getHealth`, etc.): Expose private state for `Game.java` to use in combat logic.

---

## Samurai.java
**Class Description**: Represents the second character (Samurai). Similar to Player but with different assets, speed settings, and control scheme (Arrows + Numpad).

**Functions**:
- `Samurai(int startX, int startY)`: Constructor. Sets start position and loads Samurai-specific resources.
- `loadResources()`: Loads Samurai sprite sheets. Includes error logging.
- `loadSpriteSheet(String path)`: Utility to slice sprite sheets.
- `takeDamage(int amount)`: Decrements health. handles Death state transition.
- `update()`: Manages animation ticks and frame advancement. Contains "Safety check" logic to handle null frames or index overflows.
- `setInput(int keyCode, boolean pressed)`: Maps Input. `Left/Right Arrows` for movement. `Numpad 7/8/9` (or `7/8/9`) for actions.
- `physicsUpdate()`: Updates position based on `speed` (recently increased to 20 for faster movement). Checks collisions.
- `canMoveTo(int nextX, int nextY)`: Validates if the destination rectangle is within walkable terrain.
- `draw(Graphics2D g2)`: Renders the active frame. Includes fallback logic (blue box) if frames fail to load.

---

## CollisionData.java
**Class Description**: A static container for the game's collision map (Walkable vs Blocked areas).

**Functions**:
- `static { loadMap(); }`: A static initializer block that runs automatically when the class is loaded.
- `loadMap()`: Reads `assets/collision.txt`. Parses 0s (blocked) and 1s (walkable) into a 600x800 integer array `map`. Defaults to all-walkable if the file is missing.

---

## GraphicButton.java
**Class Description**: A custom UI component for interactive buttons (Start, Quit, etc.).

**Functions**:
- `GraphicButton(String name, int x, int y, int w, int h)`: Constructor defining the button's text and dimensions.
- `setImage(Image img)`: Assigns a custom image to the button.
- `contains(int x, int y)`: Returns `true` if the coordinate is inside the button's bounds.
- `setHovered(boolean hovered)`: Updates the internal state for hover effects.
- `draw(Graphics2D g2)`: Draws the button image. If no image is set, draws a default gray rectangle with the name text.

---

## MapEditor.java
**Class Description**: A standalone tool (run via its own `main` method) to help developers visually create the `collision.txt` map.

**Functions**:
- `MapEditor()`: Constructor. Sets up the editor window, loads the background image, and creates a transparent overlay for painting.
- `paintAt(int x, int y)`: "Paints" a walkable area (White circle) onto the mask overlay at the mouse position.
- `saveMapAndExit()`: Converts the painted mask into the `0/1` text format and saves it to `assets/collision.txt`.
- `paint(Graphics g)`: Renders the background and the semi-transparent mask overlay so the user can see what they are painting.
- `main(String[] args)`: Entry point to run this specific tool.
- `MyMouseInput` (Inner Class): Handles mouse drag/click events to trigger `paintAt`.
