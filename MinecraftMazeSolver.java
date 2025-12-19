import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class MinecraftMazeSolver extends JFrame {
    private static final int CELL_SIZE = 30;
    private static final int MAZE_SIZE = 21;
    
    private CardLayout cardLayout;
    private JPanel mainContainer;
    
    private MazePanel mazePanel;
    private JComboBox<String> explorationSelector;
    private JComboBox<String> solutionSelector;
    private JButton generateButton, startButton, resetButton;
    private JLabel statusLabel, explorationStepsLabel, solutionStepsLabel;
    
    private int[][] maze;
    private int[][] weights;
    private Point start, end;
    
    private List<Point> solutionPath;
    private List<Point> explorationPath;
    private List<Point> exploredCells = new ArrayList<>();
    private boolean isAnimating = false;
    private boolean isExploring = false;
    private String selectedExploration;
    private String selectedSolution;
    
    private boolean mazeGenerated = false;

    // Cell types
    private static final int WALL = 0;
    private static final int GRASS = 1;
    private static final int MUD = 2;
    private static final int WATER = 3;
    private static final int FOOTPATH = 4;

    public MinecraftMazeSolver() {
        setTitle("Minecraft Maze Solver - Endermite & Enderman Adventure");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Setup CardLayout untuk switching halaman
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        // Tambahkan kedua halaman
        mainContainer.add(createWelcomePanel(), "WELCOME");
        mainContainer.add(createGamePanel(), "GAME");
        
        add(mainContainer);
        
        // Tampilkan halaman welcome
        cardLayout.show(mainContainer, "WELCOME");
        
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    // ========== HALAMAN WELCOME ==========
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(25, 25, 112),
                    0, getHeight(), new Color(0, 0, 0)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Decorative blocks (Minecraft style)
                drawMinecraftBlock(g2d, 50, 100, new Color(34, 139, 34));
                drawMinecraftBlock(g2d, 100, 150, new Color(139, 90, 43));
                drawMinecraftBlock(g2d, getWidth() - 150, 100, new Color(30, 144, 255));
                drawMinecraftBlock(g2d, getWidth() - 100, 150, new Color(210, 180, 140));
            }
            
            private void drawMinecraftBlock(Graphics2D g, int x, int y, Color color) {
                g.setColor(color);
                g.fillRect(x, y, 40, 40);
                g.setColor(new Color(0, 0, 0, 100));
                g.drawRect(x, y, 40, 40);
                g.drawLine(x, y, x + 40, y + 40);
                g.drawLine(x + 40, y, x, y + 40);
            }
        };
        
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(MAZE_SIZE * CELL_SIZE, MAZE_SIZE * CELL_SIZE + 200));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 20, 15, 20);
        
        // Title
        JLabel title = new JLabel("MINECRAFT MAZE SOLVER");
        title.setFont(new Font("Courier New", Font.BOLD, 36));
        title.setForeground(new Color(85, 255, 85));
        title.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(85, 255, 85), 3),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        gbc.gridy = 0;
        panel.add(title, gbc);
        
        // Subtitle
        JLabel subtitle = new JLabel("🎮 Endermite & Enderman Adventure 🎮");
        subtitle.setFont(new Font("Courier New", Font.BOLD, 20));
        subtitle.setForeground(new Color(255, 215, 0));
        gbc.gridy = 1;
        panel.add(subtitle, gbc);
        
        // Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        String[] features = {
            "🔍 Stage 1: Endermite explores the maze (BFS/DFS)",
            "🎯 Stage 2: Enderman finds optimal path (Dijkstra/A*)",
            "⚡ Real-time animation & pathfinding",
            "🗺️ Random maze generation with terrain costs",
            "📊 Performance metrics & statistics"
        };
        
        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("Courier New", Font.PLAIN, 14));
            featureLabel.setForeground(Color.WHITE);
            featureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            featureLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            infoPanel.add(featureLabel);
        }
        
        gbc.gridy = 2;
        panel.add(infoPanel, gbc);
        
        // Character Preview Panel
        JPanel charPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Endermite
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Courier New", Font.BOLD, 12));
                g2d.drawString("Endermite", 40, 15);
                drawEndermitePreview(g2d, 60, 30);
                
                // Enderman
                g2d.drawString("Enderman", 200, 15);
                drawEndermanPreview(g2d, 220, 30);
            }
            
            private void drawEndermitePreview(Graphics2D g, int x, int y) {
                g.setColor(new Color(102, 0, 153));
                g.fillRoundRect(x - 10, y, 20, 10, 5, 5);
                g.fillRect(x - 12, y - 2, 8, 4);
                g.setColor(Color.WHITE);
                g.fillRect(x - 9, y - 1, 2, 2);
            }
            
            private void drawEndermanPreview(Graphics2D g, int x, int y) {
                g.setColor(Color.BLACK);
                g.fillRect(x - 7, y - 12, 14, 10);
                g.fillRect(x - 5, y - 2, 10, 16);
                g.fillRect(x - 10, y - 2, 3, 14);
                g.fillRect(x + 7, y - 2, 3, 14);
                g.setColor(new Color(204, 0, 255));
                g.fillRect(x - 4, y - 9, 3, 4);
                g.fillRect(x + 1, y - 9, 3, 4);
            }
        };
        
        charPanel.setPreferredSize(new Dimension(300, 80));
        charPanel.setOpaque(false);
        gbc.gridy = 3;
        panel.add(charPanel, gbc);
        
        // Start Button
        JButton startBtn = new JButton("⚔ START ADVENTURE ⚔");
        startBtn.setFont(new Font("Courier New", Font.BOLD, 24));
        startBtn.setBackground(new Color(85, 85, 85));
        startBtn.setForeground(new Color(255, 215, 0));
        startBtn.setFocusPainted(false);
        startBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 3),
            BorderFactory.createEmptyBorder(15, 40, 15, 40)
        ));
        startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        startBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startBtn.setBackground(new Color(120, 120, 120));
                startBtn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                startBtn.setBackground(new Color(85, 85, 85));
                startBtn.setForeground(new Color(255, 215, 0));
            }
        });
        
        startBtn.addActionListener(e -> {
            cardLayout.show(mainContainer, "GAME");
        });
        
        gbc.gridy = 4;
        gbc.insets = new Insets(30, 20, 20, 20);
        panel.add(startBtn, gbc);
        
        // Credits
        JLabel credits = new JLabel("Created with ❤ for Pathfinding Visualization");
        credits.setFont(new Font("Courier New", Font.ITALIC, 11));
        credits.setForeground(new Color(180, 180, 180));
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 20, 10, 20);
        panel.add(credits, gbc);
        
        return panel;
    }
    
    // ========== HALAMAN GAME ==========
    private JPanel createGamePanel() {
        JPanel gamePanel = new JPanel(new BorderLayout());
        
        // Control Panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(new Color(139, 90, 43));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Row 1: Generate Button
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        generateButton = createMinecraftButton("Generate New Maze");
        generateButton.setFont(new Font("Courier New", Font.BOLD, 16));
        controlPanel.add(generateButton, gbc);
        
        // Row 2: Dropdown Eksplorasi
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel exploLabel = new JLabel("Exploration (Endermite): ");
        exploLabel.setForeground(Color.WHITE);
        exploLabel.setFont(new Font("Courier New", Font.BOLD, 12));
        controlPanel.add(exploLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        String[] explorationAlgs = {"BFS (Breadth-First)", "DFS (Depth-First)"};
        explorationSelector = new JComboBox<>(explorationAlgs);
        explorationSelector.setFont(new Font("Courier New", Font.BOLD, 12));
        explorationSelector.setBackground(Color.BLACK);
        explorationSelector.setForeground(Color.WHITE);
        explorationSelector.setEnabled(false);
        // Custom renderer untuk dropdown list
        explorationSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(85, 85, 85) : Color.BLACK);
                setForeground(Color.WHITE);
                setFont(new Font("Courier New", Font.BOLD, 12));
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
        controlPanel.add(explorationSelector, gbc);
        
        // Row 3: Dropdown Solusi
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel solutionLabel = new JLabel("Solution (Enderman): ");
        solutionLabel.setForeground(Color.WHITE);
        solutionLabel.setFont(new Font("Courier New", Font.BOLD, 12));
        controlPanel.add(solutionLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        String[] solutionAlgs = {"Dijkstra (Cost Optimal)", "A* (Cost Optimal & Fast)"};
        solutionSelector = new JComboBox<>(solutionAlgs);
        solutionSelector.setFont(new Font("Courier New", Font.BOLD, 12));
        solutionSelector.setBackground(Color.BLACK);
        solutionSelector.setForeground(Color.WHITE);
        solutionSelector.setEnabled(false);
        // Custom renderer untuk dropdown list
        solutionSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(85, 85, 85) : Color.BLACK);
                setForeground(Color.WHITE);
                setFont(new Font("Courier New", Font.BOLD, 12));
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
        controlPanel.add(solutionSelector, gbc);
        
        // Row 4: Start & Reset Buttons
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        startButton = createMinecraftButton("Start Solving!");
        startButton.setFont(new Font("Courier New", Font.BOLD, 14));
        startButton.setEnabled(false);
        controlPanel.add(startButton, gbc);
        
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        resetButton = createMinecraftButton("Reset");
        controlPanel.add(resetButton, gbc);
        
        // Status Panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(139, 90, 43));
        
        statusLabel = new JLabel("Click 'Generate New Maze' to begin.");
        statusLabel.setFont(new Font("Courier New", Font.BOLD, 12));
        statusLabel.setForeground(Color.WHITE);
        
        explorationStepsLabel = new JLabel("");
        explorationStepsLabel.setFont(new Font("Courier New", Font.BOLD, 11));
        explorationStepsLabel.setForeground(new Color(200, 150, 255));
        
        solutionStepsLabel = new JLabel("");
        solutionStepsLabel.setFont(new Font("Courier New", Font.BOLD, 11));
        solutionStepsLabel.setForeground(Color.YELLOW);
        
        statusPanel.add(statusLabel);
        statusPanel.add(explorationStepsLabel);
        statusPanel.add(solutionStepsLabel);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.CENTER);
        topPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Info Panel (Legend)
        JPanel infoPanel = createInfoPanel();
        
        // Maze Panel
        mazePanel = new MazePanel();
        
        gamePanel.add(topPanel, BorderLayout.NORTH);
        gamePanel.add(mazePanel, BorderLayout.CENTER);
        gamePanel.add(infoPanel, BorderLayout.SOUTH);
        
        // Button Actions
        generateButton.addActionListener(e -> generateMaze());
        startButton.addActionListener(e -> solveMaze());
        resetButton.addActionListener(e -> reset());
        
        return gamePanel;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 10, 5));
        panel.setBackground(new Color(60, 60, 60));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(createLegendLabel("Wall", new Color(64, 64, 64)));
        panel.add(createLegendLabel("Grass (1)", new Color(34, 139, 34)));
        panel.add(createLegendLabel("Mud (5)", new Color(139, 90, 43)));
        panel.add(createLegendLabel("Water (10)", new Color(30, 144, 255)));
        panel.add(createLegendLabel("Footpath (0)", new Color(210, 180, 140)));
        
        return panel;
    }
    
    private JPanel createLegendLabel(String text, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(60, 60, 60));
        
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        JLabel label = new JLabel(" " + text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Courier New", Font.BOLD, 11));
        
        panel.add(colorBox, BorderLayout.WEST);
        panel.add(label, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createMinecraftButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Courier New", Font.BOLD, 12));
        button.setBackground(new Color(85, 85, 85));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return button;
    }
    
    private void generateMaze() {
        maze = new int[MAZE_SIZE][MAZE_SIZE];
        weights = new int[MAZE_SIZE][MAZE_SIZE];
        solutionPath = null;
        exploredCells.clear();
        explorationPath = null;
        
        for (int i = 0; i < MAZE_SIZE; i++) {
            Arrays.fill(maze[i], WALL);
        }

        start = new Point(1, 1);
        maze[start.y][start.x] = FOOTPATH;
        weights[start.y][start.x] = 0;

        List<Wall> walls = new ArrayList<>();
        addWallsToList(start.y, start.x, walls);

        Random rand = new Random();

        while (!walls.isEmpty()) {
            Wall wall = walls.remove(rand.nextInt(walls.size()));
            int ny = wall.y;
            int nx = wall.x;

            List<Point> pathNeighbors = new ArrayList<>();
            List<Point> wallNeighbors = new ArrayList<>();

            int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};

            for (int[] dir : dirs) {
                int cy = ny + dir[0];
                int cx = nx + dir[1];
                if (isValid(cy, cx)) {
                    if (maze[cy][cx] != WALL) {
                        pathNeighbors.add(new Point(cx, cy));
                    } else {
                        wallNeighbors.add(new Point(cx, cy));
                    }
                }
            }

            if (pathNeighbors.size() == 1 && wallNeighbors.size() > 0) {
                assignRandomTerrain(ny, nx, rand);

                Point nextCell = wallNeighbors.get(rand.nextInt(wallNeighbors.size()));
                if (maze[nextCell.y][nextCell.x] == WALL) {
                    assignRandomTerrain(nextCell.y, nextCell.x, rand);
                    addWallsToList(nextCell.y, nextCell.x, walls);
                }
            }
        }

        end = findFurthestPoint(start);
        maze[end.y][end.x] = FOOTPATH;
        weights[end.y][end.x] = 0;

        maze[start.y][start.x] = FOOTPATH;
        weights[start.y][start.x] = 0;

        mazeGenerated = true;
        explorationSelector.setEnabled(true);
        solutionSelector.setEnabled(true);
        startButton.setEnabled(true);
        
        statusLabel.setText("Maze generated! Select algorithms and click 'Start Solving!'");
        explorationStepsLabel.setText("");
        solutionStepsLabel.setText("");
        mazePanel.repaint();
    }

    private void assignRandomTerrain(int y, int x, Random rand) {
        int terrainType = rand.nextInt(100);

        if (terrainType < 30) {
            maze[y][x] = GRASS;
            weights[y][x] = 1;
        } else if (terrainType < 55) {
            maze[y][x] = MUD;
            weights[y][x] = 5;
        } else if (terrainType < 70) {
            maze[y][x] = WATER;
            weights[y][x] = 10;
        } else {
            maze[y][x] = FOOTPATH;
            weights[y][x] = 0;
        }
    }

    private Point findFurthestPoint(Point start) {
        Queue<Point> queue = new LinkedList<>();
        Map<Point, Integer> distances = new HashMap<>();
        queue.add(start);
        distances.put(start, 0);
        
        Point furthest = start;
        int maxDist = 0;
        
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            int dist = distances.get(current);
            
            if (dist > maxDist) {
                maxDist = dist;
                furthest = current;
            }
            
            for (int[] dir : dirs) {
                int ny = current.y + dir[0];
                int nx = current.x + dir[1];
                Point next = new Point(nx, ny);
                
                if (isValid(ny, nx) && maze[ny][nx] != WALL && !distances.containsKey(next)) {
                    distances.put(next, dist + 1);
                    queue.add(next);
                }
            }
        }
        
        return furthest;
    }

    private void addWallsToList(int y, int x, List<Wall> walls) {
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] dir : dirs) {
            int ny = y + dir[0];
            int nx = x + dir[1];
            if (isValid(ny, nx) && maze[ny][nx] == WALL) {
                walls.add(new Wall(ny, nx));
            }
        }
    }

    private boolean isValid(int y, int x) {
        return y > 0 && y < MAZE_SIZE - 1 && x > 0 && x < MAZE_SIZE - 1;
    }

    private void solveMaze() {
        if (!mazeGenerated) {
            statusLabel.setText("Generate maze first!");
            return;
        }
        if (isAnimating) return;

        selectedExploration = (String) explorationSelector.getSelectedItem();
        selectedSolution = (String) solutionSelector.getSelectedItem();
        
        solutionPath = null;
        exploredCells.clear();

        isExploring = true;
        
        String exploType = selectedExploration.startsWith("DFS") ? "DFS" : "BFS";
        statusLabel.setText("Stage 1: Endermite exploring with " + exploType + "...");
        explorationStepsLabel.setText("");
        solutionStepsLabel.setText("");
        
        if (exploType.equals("DFS")) {
            explorationPath = exploreDFS();
        } else {
            explorationPath = exploreBFS();
        }
        
        if (explorationPath == null || explorationPath.isEmpty() || !explorationPath.contains(end)) {
            statusLabel.setText("Exploration failed. No path to End!");
            explorationStepsLabel.setText("");
            isExploring = false;
            mazePanel.repaint();
            return;
        }

        animateExploration();
    }
    
    private List<Point> exploreBFS() {
        List<Point> visitedSequence = new ArrayList<>();
        Queue<Point> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();
    
        queue.add(start);
        visited.add(start);
    
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
    
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (!visitedSequence.contains(current)) {
                visitedSequence.add(current);
            }
            
            if (current.equals(end)) {
                break;
            }
    
            for (int[] dir : dirs) {
                int ny = current.y + dir[0];
                int nx = current.x + dir[1];
                Point next = new Point(nx, ny);
    
                if (isValid(ny, nx) && maze[ny][nx] != WALL && !visited.contains(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return visitedSequence;
    }
    
    private List<Point> exploreDFS() {
        List<Point> visitedSequence = new ArrayList<>();
        Stack<Point> stack = new Stack<>();
        Set<Point> visited = new HashSet<>();
    
        stack.push(start);
        visited.add(start);
    
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
    
        while (!stack.isEmpty()) {
            Point current = stack.pop();
            if (!visitedSequence.contains(current)) {
                 visitedSequence.add(current);
            }
            
            if (current.equals(end)) {
                break;
            }
    
            for (int i = dirs.length - 1; i >= 0; i--) {
                int ny = current.y + dirs[i][0];
                int nx = current.x + dirs[i][1];
                Point next = new Point(nx, ny);
    
                if (isValid(ny, nx) && maze[ny][nx] != WALL && !visited.contains(next)) {
                    visited.add(next);
                    stack.push(next);
                }
            }
        }
        return visitedSequence;
    }
    
    private void animateExploration() {
        isAnimating = true;
        mazePanel.currentStep = -1;
        javax.swing.Timer timer = new javax.swing.Timer(50, null);
        final int[] index = {0};

        timer.addActionListener(e -> {
            if (index[0] < explorationPath.size()) {
                Point current = explorationPath.get(index[0]);
                mazePanel.currentStep = index[0];
                if (!current.equals(start) && !current.equals(end)) {
                    exploredCells.add(current);
                }
                mazePanel.repaint();
                index[0]++;
            } else {
                timer.stop();
                isAnimating = false;
                isExploring = false;
                
                explorationStepsLabel.setText(" | Exploration Steps: " + explorationPath.size());
                statusLabel.setText("Exploration complete! Enderman finding optimal path...");
                
                startSolving();
            }
        });
        timer.start();
    }
    
    private void startSolving() {
        exploredCells.clear();
        mazePanel.repaint();

        long startTime = System.currentTimeMillis();
        
        if (selectedSolution.startsWith("Dijkstra")) {
            solutionPath = solveDijkstra();
        } else if (selectedSolution.startsWith("A*")) {
            solutionPath = solveAStar();
        }

        long endTime = System.currentTimeMillis();
        
        if (solutionPath != null) {
            int totalCost = calculatePathCost(solutionPath);
            String algoName = selectedSolution.split(" ")[0];
            statusLabel.setText("Stage 2: Enderman solved with " + algoName + " (Time: " + (endTime - startTime) + "ms)");
            solutionStepsLabel.setText(" | Path Cost: " + totalCost + " | Steps: " + solutionPath.size());

            animateSolution();
        } else {
            statusLabel.setText("Stage 2: Solution not found!");
            solutionStepsLabel.setText("");
        }
    }
    
    private void animateSolution() {
        isAnimating = true;
        mazePanel.currentStep = -1;
        javax.swing.Timer timer = new javax.swing.Timer(80, null);
        final int[] index = {0};
        
        timer.addActionListener(e -> {
            if (index[0] < solutionPath.size()) {
                mazePanel.currentStep = index[0];
                mazePanel.repaint();
                index[0]++;
            } else {
                timer.stop();
                isAnimating = false;
                statusLabel.setText("✓ Enderman reached the goal!");
            }
        });
        
        timer.start();
    }

    private int calculatePathCost(List<Point> path) {
        int cost = 0;
        for (Point p : path) {
            cost += weights[p.y][p.x];
        }
        return cost;
    }
    
    private List<Point> solveDijkstra() {
        PriorityQueue<Node> pq = new PriorityQueue<>();
        Map<Point, Integer> dist = new HashMap<>();
        Map<Point, Point> parent = new HashMap<>();

        pq.add(new Node(start, 0));
        dist.put(start, 0);
        
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        
        while (!pq.isEmpty()) {
            Node current = pq.poll();
            
            if (current.point.equals(end)) {
                return reconstructPath(parent, end);
            }
            
            if (dist.containsKey(current.point) && current.cost > dist.get(current.point)) {
                continue;
            }
            
            for (int[] dir : dirs) {
                int ny = current.point.y + dir[0];
                int nx = current.point.x + dir[1];
                Point next = new Point(nx, ny);
                
                if (isValid(ny, nx) && maze[ny][nx] != WALL) {
                    int newDist = dist.get(current.point) + weights[ny][nx];
                    
                    if (!dist.containsKey(next) || newDist < dist.get(next)) {
                        dist.put(next, newDist);
                        parent.put(next, current.point);
                        pq.add(new Node(next, newDist));
                    }
                }
            }
        }
        return null;
    }

    private List<Point> solveAStar() {
        PriorityQueue<AStarNode> pq = new PriorityQueue<>();
        Map<Point, Integer> gScore = new HashMap<>();
        Map<Point, Point> parent = new HashMap<>();
        Set<Point> closed = new HashSet<>();
        
        pq.add(new AStarNode(start, 0, heuristic(start, end)));
        gScore.put(start, 0);
        
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        
        while (!pq.isEmpty()) {
            AStarNode current = pq.poll();
            
            if (current.point.equals(end)) {
                return reconstructPath(parent, end);
            }
            
            if (closed.contains(current.point)) {
                continue;
            }
            
            closed.add(current.point);
            
            for (int[] dir : dirs) {
                int ny = current.point.y + dir[0];
                int nx = current.point.x + dir[1];
                Point next = new Point(nx, ny);
                
                if (isValid(ny, nx) && maze[ny][nx] != WALL && !closed.contains(next)) {
                    int tentativeG = gScore.get(current.point) + weights[ny][nx];
                    
                    if (!gScore.containsKey(next) || tentativeG < gScore.get(next)) {
                        gScore.put(next, tentativeG);
                        parent.put(next, current.point);
                        
                        int f = tentativeG + heuristic(next, end);
                        pq.add(new AStarNode(next, tentativeG, f));
                    }
                }
            }
        }
        return null;
    }

    private int heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private List<Point> reconstructPath(Map<Point, Point> parent, Point end) {
        List<Point> path = new ArrayList<>();
        Point current = end;
        
        while (current != null) {
            path.add(0, current);
            current = parent.get(current);
        }
        
        return path;
    }

    private void reset() {
        solutionPath = null;
        explorationPath = null;
        exploredCells.clear();
        isAnimating = false;
        isExploring = false;
        mazePanel.currentStep = -1;
        mazePanel.repaint();
        statusLabel.setText("Reset complete. Generate a new maze or start again!");
        explorationStepsLabel.setText("");
        solutionStepsLabel.setText("");
    }

    class Wall {
        int y, x;
        Wall(int y, int x) {
            this.y = y;
            this.x = x;
        }
    }

    class Node implements Comparable<Node> {
        Point point;
        int cost;
        Node(Point point, int cost) {
            this.point = point;
            this.cost = cost;
        }
        public int compareTo(Node o) {
            return Integer.compare(this.cost, o.cost);
        }
    }

    class AStarNode implements Comparable<AStarNode> {
        Point point;
        int g, f;
        AStarNode(Point point, int g, int f) {
            this.point = point;
            this.g = g;
            this.f = f;
        }
        public int compareTo(AStarNode o) {
            return Integer.compare(this.f, o.f);
        }
    }

    class MazePanel extends JPanel {
        int currentStep = -1;
        
        public MazePanel() {
            setPreferredSize(new Dimension(MAZE_SIZE * CELL_SIZE, MAZE_SIZE * CELL_SIZE));
            setBackground(Color.BLACK);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (maze == null) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Courier New", Font.BOLD, 20));
                String msg = "Click 'Generate New Maze' to start!";
                
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(msg)) / 2;
                int y = getHeight() / 2;
                
                g2d.drawString(msg, x, y);
                return;
            }
            
            for (int i = 0; i < MAZE_SIZE; i++) {
                for (int j = 0; j < MAZE_SIZE; j++) {
                    int x = j * CELL_SIZE;
                    int y = i * CELL_SIZE;
                    
                    Color cellColor;
                    switch (maze[i][j]) {
                        case WALL:
                            cellColor = new Color(64, 64, 64);
                            break;
                        case GRASS:
                            cellColor = new Color(34, 139, 34);
                            break;
                        case MUD:
                            cellColor = new Color(139, 90, 43);
                            break;
                        case WATER:
                            cellColor = new Color(30, 144, 255);
                            break;
                        case FOOTPATH:
                            cellColor = new Color(210, 180, 140);
                            break;
                        default:
                            cellColor = Color.WHITE;
                    }
                    
                    g2d.setColor(cellColor);
                    g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    
                    g2d.setColor(new Color(0, 0, 0, 100));
                    g2d.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
            
            if (isExploring) {
                g2d.setColor(new Color(102, 0, 153, 70));
                for (Point p : exploredCells) {
                    if (p != null && !p.equals(start) && !p.equals(end)) {
                        g2d.fillRect(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
            
            if (solutionPath != null && !isExploring && currentStep >= 0) {
                g2d.setStroke(new BasicStroke(3));
                g2d.setColor(new Color(255, 0, 255, 120));
                
                for (int i = 0; i < Math.min(currentStep, solutionPath.size()); i++) {
                    Point p = solutionPath.get(i);
                    int x = p.x * CELL_SIZE + CELL_SIZE / 2;
                    int y = p.y * CELL_SIZE + CELL_SIZE / 2;
                    
                    if (i > 0) {
                        Point prev = solutionPath.get(i - 1);
                        int px = prev.x * CELL_SIZE + CELL_SIZE / 2;
                        int py = prev.y * CELL_SIZE + CELL_SIZE / 2;
                        g2d.drawLine(px, py, x, y);
                    }
                    
                    g2d.fillOval(x - 3, y - 3, 6, 6);
                }
            }
            
            if (start != null) {
                int sx = start.x * CELL_SIZE;
                int sy = start.y * CELL_SIZE;
                
                g2d.setColor(new Color(0, 255, 0, 200));
                g2d.fillOval(sx + 8, sy + 8, CELL_SIZE - 16, CELL_SIZE - 16);
                
                g2d.setColor(new Color(0, 200, 0));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawOval(sx + 8, sy + 8, CELL_SIZE - 16, CELL_SIZE - 16);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Courier New", Font.BOLD, 10));
                g2d.drawString("S", sx + CELL_SIZE/2 - 4, sy + CELL_SIZE/2 + 4);
            }
            
            if (end != null) {
                int ex = end.x * CELL_SIZE;
                int ey = end.y * CELL_SIZE;
                
                g2d.setColor(new Color(255, 0, 0, 200));
                g2d.fillOval(ex + 8, ey + 8, CELL_SIZE - 16, CELL_SIZE - 16);
                
                g2d.setColor(new Color(200, 0, 0));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawOval(ex + 8, ey + 8, CELL_SIZE - 16, CELL_SIZE - 16);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Courier New", Font.BOLD, 10));
                g2d.drawString("E", ex + CELL_SIZE/2 - 4, ey + CELL_SIZE/2 + 4);
            }
            
            Point characterPos = null;
            
            if (isExploring && explorationPath != null && currentStep >= 0 && currentStep < explorationPath.size()) {
                characterPos = explorationPath.get(currentStep);
            } else if (!isExploring && solutionPath != null && currentStep >= 0 && currentStep < solutionPath.size()) {
                characterPos = solutionPath.get(currentStep);
            } else if (start != null) {
                characterPos = start;
            }
            
            if (characterPos != null) {
                if (isExploring) {
                    drawEndermite(g2d, characterPos.x * CELL_SIZE, characterPos.y * CELL_SIZE);
                } else {
                    drawEnderman(g2d, characterPos.x * CELL_SIZE, characterPos.y * CELL_SIZE);
                }
            }
        }
        
        private void drawEnderman(Graphics2D g, int x, int y) {
            g.setColor(Color.BLACK);
            g.fillRect(x + 13, y + 3, 14, 10);
            g.fillRect(x + 15, y + 13, 10, 16);
            g.fillRect(x + 10, y + 13, 3, 14);
            g.fillRect(x + 27, y + 13, 3, 14);
            g.fillRect(x + 16, y + 29, 4, 8);
            g.fillRect(x + 22, y + 29, 4, 8);
            
            g.setColor(new Color(204, 0, 255));
            g.fillRect(x + 16, y + 6, 3, 4);
            g.fillRect(x + 21, y + 6, 3, 4);
            
            g.setColor(new Color(204, 0, 255, 100));
            g.fillRect(x + 15, y + 5, 5, 6);
            g.fillRect(x + 20, y + 5, 5, 6);
        }
        
        private void drawEndermite(Graphics2D g, int x, int y) {
            g.setColor(new Color(102, 0, 153));
            g.fillRoundRect(x + 10, y + 15, 20, 10, 5, 5);
            g.fillRect(x + 8, y + 13, 8, 4);
            
            g.setColor(Color.WHITE);
            g.fillRect(x + 11, y + 14, 2, 2);
            
            g.setColor(Color.BLACK);
            g.drawLine(x + 12, y + 13, x + 10, y + 10);
            g.drawLine(x + 14, y + 13, x + 16, y + 10);
            g.drawLine(x + 15, y + 25, x + 13, y + 28);
            g.drawLine(x + 25, y + 25, x + 27, y + 28);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MinecraftMazeSolver().setVisible(true);
        });
    }
}