# Java Game Project Documentation

## 1. Project Overview
This project is a 2D Top-Down Role-Playing Game (RPG) engine built entirely in **core Java (AWT)** without external game libraries. It features a custom game loop, an object-oriented architecture, animated backgrounds, and a pixel-perfect grid-based collision system generated via a custom Map Editor tool.

---

## 2. Architecture & Design Pattern
The project follows a **Component-Based Object-Oriented Design**. Instead of a monolithic single file, the logic is distributed across specialized classes:
*   **Game Control**: `Main.java` & `Game.java`
*   **Entities**: `Player.java`
*   **UI Components**: `GraphicButton.java`
*   **Data Management**: `CollisionData.java`
*   **Tools**: `MapEditor.java`

---

## 3. Detailed File Breakdown

### A. `Main.java` (Entry Point)
**Purpose**: Starts the application.
*   **Logic**: It contains the standard `public static void main` method.
*   **Action**: It simply instantiates the `Game` class (`new Game()`), which launches the GUI window.

### B. `Game.java` (The Engine)
**Purpose**: Manages the game window, game loop, rendering, and input handling.
*   **Key Components**:
    *   **`Frame`**: Extends Java's AWT `Frame` to create the native OS window.
    *   **`Thread gameLoopThread`**: A dedicated thread that runs the game logic separately from the UI event thread.
    *   **`Image[] gameBgImages`**: Stores the 4 frames of the background animation.
    *   **`Player player`**: An instance of the `Player` class representing the user.
*   **Core Logic**:
    *   **`constructor`**: Sets window size (800x600), initializes the Player at (350, 250), loads resources, and sets up Event Listeners (Mouse & Keyboard).
    *   **`startAnimationLoop()`**: This is the heartbeat of the game. It runs a `while(isPlaying)` loop. Inside, it calls `repaint()` to draw the screen and then `Thread.sleep(18)` to pause for ~18 milliseconds. This creates a smooth frame rate of approximately **55 FPS**.
    *   **`paint(Graphics g)`**: The rendering pipeline.
        1.  **Anti-aliasing**: Sets `RenderingHints` for smooth graphics.
        2.  **Background**: Draws one of the 4 animation frames based on `currentBgFrame`.
        3.  **State Check**: If `isPlaying` is false, it draws the Menu Buttons. If true, it draws the `Player`.
    *   **`loadResources()`**: Loads images (`bg1.png`, `character.png`, buttons) and audio (`music.wav`) from the `assets/` folder.

### C. `Player.java` (The Entity)
**Purpose**: Encapsulates all logic related to the character.
*   **Attributes**: Position (`x, y`), dimensions (`width, height` = 32x32), and `speed`.
*   **`move(int keyCode)`**:
    1.  Calculates the **Proposed Position** (`nextX`, `nextY`) based on WASD input.
    2.  **Collision Check**: Calls `canMoveTo(nextX, nextY)`.
    3.  If `canMoveTo` returns `true`, it updates the actual `x, y`. If `false`, it prints "Blocked" and does not move.
*   **`canMoveTo(x, y)`**: Checks all **four corners** of the player's bounding box against the collision map. This ensures the player doesn't clip through walls partially.
*   **`isWalkable(x, y)`**: Queries the `CollisionData` class to see if a specific pixel coordinate is valid (1) or blocked (0).

### D. `CollisionData.java` (The Data)
**Purpose**: A static data container for the map.
*   **`int[][] map`**: A 2D array of size [600][800] representing the game screen.
    *   `0`: Blocked Area
    *   `1`: Walkable Area
*   **`static { loadMap() }`**: Automatically runs when the game starts.
*   **`loadMap()`**: Reads the `assets/collision.txt` file. This file contains 600 lines of 0s and 1s. It parses these characters to populate the `map` array.

### E. `GraphicButton.java` (The UI)
**Purpose**: A reusable button component.
*   **Logic**: Holds an `Image` and a `Rectangle` (collision box).
*   **`contains(x, y)`**: Checks if the mouse is inside the button.
*   **`draw(g)`**: Renders the image if valid, or draws a fallback gray rectangle with text if the image is missing.

### F. `MapEditor.java` (The Tool)
**Purpose**: A developer tool to create the `collision.txt` file.
*   **Workflow**:
    1.  Loads `bg1.png`.
    2.  Creates a dark overlay (mask).
    3.  Allows the user to paint "White" spots on the mask using the mouse.
    4.  On close, it scans every pixel. White pixels become `1`, Dark pixels become `0`.
    5.  Saves the result to `assets/collision.txt`.

---

## 6. Execution Flow: From Start to Finish

Here is the step-by-step lifecycle of the application:

### Step 1: The Launch (`Main.java`)
*   You run `java Main`.
*   The Java Virtual Machine (JVM) calls `Main.main()`.
*   **Action**: It immediately executes `new Game()`.

### Step 2: Initialization (`Game.java` Constructor)
*   The `Game` object is created.
*   **Superclass**: It calls `Frame` code to create the window.
*   **Player**: It executes `new Player()`.
*   **Layout**: It creates the 3 buttons (Start, Pause, Quit).
*   **Events**: It attaches the `MouseListener` (for clicks) and `KeyListener` (for WASD).
*   **Resources**: It reads your disk to load PNGs and WAVs into memory.
*   **Visibility**: `setVisible(true)` is called, and the window pops up on your screen.

### Step 3: The Static Load (`CollisionData.java`)
*   At some point during startup (usually when `Player` is accessed), the `CollisionData` class is loaded.
*   The `static { ... }` block triggers automatically.
*   **Action**: It opens `assets/collision.txt`, reads line-by-line, and fills the `map[][]` array with 0s and 1s.

### Step 4: The Menu Loop (Idle State)
*   The game is now running, but `isPlaying` is `false`.
*   The AWT system calls `paint()` whenever the window needs refreshing.
*   `paint()` sees `isPlaying == false`, so it draws the **Static Menu Background** and the **Buttons**.
*   It waits there, doing nothing, until you click.

### Step 5: User Interaction (Clicking Start)
*   You click the mouse.
*   `MyMouseListener` fires `mousePressed`.
*   The code checks: "Was this click inside the Start Button's rectangle?"
*   **Result**: It sets `isPlaying = true` and calls `startAnimationLoop()`.

### Step 6: The Game Loop (Logic & Animation)
*   `startAnimationLoop()` spawns a **New Thread**.
*   This thread enters a `while(true)` loop.
    1.  **Frame Update**: It changes the background image index (0->1->2->3->0...).
    2.  **Repaint**: It asks the window to redraw.
    3.  **Sleep**: It pauses for 18ms.
*   This creates the visual illusion of movement/animation.

### Step 7: Player Movement (Input Handling)
*   You press 'W'.
*   `KeyListener` fires `keyPressed`.
*   It calls `player.move()`.
*   **Logic**:
    *   Player checks: "If I move up, will I hit a wall?"
    *   It looks at `CollisionData.map` at the target coordinates.
    *   If it finds a `0`, it prints "Blocked".
    *   If it finds a `1`, it updates `player.y`.
*   `repaint()` is called immediately to show the player in the new spot.

