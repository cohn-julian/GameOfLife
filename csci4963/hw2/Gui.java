package csci4963.hw2;

//import javax.print.attribute.standard.PresentationDirection;
import javax.swing.*;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Field; 



public class Gui extends JFrame {
    private JMenuBar mb;
    private JMenu file, configuration, run;
    private JMenuItem selectSeed, saveTick;
    private JMenuItem setDeadColor, setLiveColor, setOutputPattern, setGridSize, setNumTicks;
    private JMenuItem nextTick, previousTick, runSetTicks;

    private int height = 8;
    private int width = 8;
    private Board b;
    private boolean[][] seed;
    private int numLive = 0; // For statistics
    private int numDead = 0; // far statistics

    private Color dead = Color.GRAY;
    private Color live = Color.BLACK;
    private String deadColor = "GRAY";
    private String liveColor = "BLACK";
    private int numTicks;
    private int currentTick = 0;
    private String outputPrefix = "";
    private boolean isStarted;
    private JPanel seedButtons; // Layout with buttons for graphicaly selectinng seed
    private JPanel mainView; //Layout with Automata
    private JPanel uiButtons; // buttons for comonly used actions
    private JPanel combinedView; //Buttons on top, seedButtons or mainView on bottom. 


    public Gui() {
        loadSettingsFile();
        isStarted = false;
        setSize(1000, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        seed = new boolean[height][width];
        //Set all seed spots to false (dead)
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                seed[i][j] = false;
            }
        }
        startUI();

        //MENU BAR STUFF
        mb = new JMenuBar();
        setJMenuBar(mb);
        file = new JMenu("file");
        configuration = new JMenu("configuration");
        run = new JMenu("run");
        mb.add(file);
        mb.add(configuration);
        mb.add(run);
        selectSeed = new JMenuItem("select seed file");
        selectSeed.addActionListener((ActionListener) new selectSeedActon());
        saveTick = new JMenuItem("save current tick");
        saveTick.addActionListener((ActionListener) new saveTickAction());
        setDeadColor = new JMenuItem("set Dead color");
        setLiveColor = new JMenuItem("set Live Color");
        setOutputPattern = new JMenuItem("set output pattern");
        setGridSize = new JMenuItem("set default grid size");
        setNumTicks = new JMenuItem("set number of ticks for default run");
        //Set the color of the dead automata. 
        setDeadColor.addActionListener(new ActionListener(){
            /**
             *  changes Color dead in memory and in settings.txt
             */
            @Override
            public void actionPerformed(ActionEvent e){
                try{
                    deadColor = JOptionPane.showInputDialog("Enter Dead UI Color");
                    Field field = Class.forName("java.awt.Color").getField(deadColor);
                    dead = (Color) field.get(null);
                    startUI();
                    saveConfigToFile();
                }catch (Exception ex){
                    dead = Color.BLACK;
                    System.out.println("ERROR: setting dead color failed. using default");
                }
            }
        });
        //Set the Color of the live automata
        setLiveColor.addActionListener(new ActionListener(){
            /**
             *  changes Color live in memory and in settings.txt
             */
            @Override
            public void actionPerformed(ActionEvent e){
                try{
                    liveColor = JOptionPane.showInputDialog("Enter Live UI Color");
                    Field field = Class.forName("java.awt.Color").getField(liveColor);
                    live = (Color) field.get(null);
                    saveConfigToFile();
                    startUI();
                }catch (Exception ex){
                    live = Color.BLUE;
                    System.out.println("ERROR: setting live color failed. using default");
                }
            }
        });
        //Set the ouput prefix for saving files
        setOutputPattern.addActionListener(new ActionListener(){
            /**
             *  changes outputPrefix string and saves to settings.txt
             */
            @Override
            public void actionPerformed(ActionEvent e){
                outputPrefix = JOptionPane.showInputDialog("Enter output file prefix");
                saveConfigToFile();
            }
        });
        //Set grid width and height
        setGridSize.addActionListener(new ActionListener(){
            /**
             *  resets seed with correct height and width (changing all automata to Dead), redraws UI, and saves dimensions to settings.txt
             */
            @Override
            public void actionPerformed(ActionEvent e){
                String height_string = JOptionPane.showInputDialog("Enter Grid Height");
                String width_string = JOptionPane.showInputDialog("Enter Grid Width");
                try{
                    height = Integer.parseInt(height_string);
                    width = Integer.parseInt(width_string);
                    seed = new boolean[height][width];
                    for (int i=0; i<height; i++){
                        for (int j=0; j<width; j++){
                            seed[i][j] = false;
                        }
                    }
                    b = new Board(height, width, seed);
                    saveConfigToFile();
                    startUI();
                }catch (Exception ex){
                    System.out.println("ERROR: setting width or height failed.");
                }
            }
        });
        //Set default number of ticks for running GameOfLife
        setNumTicks.addActionListener(new ActionListener(){
            /**
             *  changes number of ticks for deafault run, reloads UI to change button, and saves change to settings.txt
             */
            @Override
            public void actionPerformed(ActionEvent e){
                String numTicks_string = JOptionPane.showInputDialog("Enter number of ticks desired");
                try{
                    numTicks = Integer.parseInt(numTicks_string);
                    saveConfigToFile();
                    startUI();
                }catch (Exception ex){
                    System.out.println("ERROR: Setting number of ticks failed");
                }
            }
        });
        nextTick = new JMenuItem("next tick");
        nextTick.addActionListener((ActionListener) new nextTickAction());
        previousTick = new JMenuItem("previous tick");
        previousTick.addActionListener( (ActionListener) new previousTickAction());
        runSetTicks = new JMenuItem("run");
        runSetTicks.addActionListener( (ActionListener) new runAction());
        file.add(selectSeed);
        file.add(saveTick);
        configuration.add(setNumTicks);
        configuration.add(setDeadColor);
        configuration.add(setLiveColor);
        configuration.add(setOutputPattern);
        configuration.add(setGridSize);
        run.add(nextTick);
        run.add(previousTick);
        run.add(runSetTicks);


    }
    /**
     * Driver for show game of life
     *  removes current UI, creates new set of squares, and adds it to UI.
     * @param showSeed if true present board's seed, else use the next tick
     * 
     */
    public void presentGameOfLife(boolean showSeed){
        currentTick++;
        getContentPane().removeAll(); // Removes all JPanels from JFrame
        //If showSeed is true, use b.seed() instead of b.tick()
        if (showSeed){
            mainView = showGameOfLife(b.getSeed());
        }
        else {
            mainView = showGameOfLife(b.tick());
        }
        uiButtons = uiButtonsLayout();
        combinedView = combinedLayout(mainView, uiButtons);
        add(combinedView);
        revalidate(); //Reloads UI
        repaint();
    }
    
    /**
     * Function for starting UI with seedButtons which allow user to change the seed
     *  removes all pane's, creates seedButtons, adds that to UI. 
     */
    private void startUI(){
        currentTick = 0;
        getContentPane().removeAll(); // Removes all JPanels from JFrame
        uiButtons = uiButtonsLayout();
        seedButtons = seedButtonsLayout();
        combinedView = combinedLayout(seedButtons, uiButtons);
        add(combinedView);
        revalidate(); //Reloads UI
        repaint();
    }

    /**
     * Creates buttons with color based on board[][] and returns gridLayout view with those buttons 
     * @returns JPanel with Buttons with no ActionListener and Color based on board[][] values
     */
    private JPanel showGameOfLife(boolean[][] board) {
        JPanel gameOfLife = new JPanel();
        gameOfLife.setLayout(new GridLayout(height, width)); //Set grid layout
        numLive = numDead = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                JButton button = new JButton();
                button.setOpaque(true);
                button.setBorderPainted(false);
                // Set the background color based on board[i][j]
                if (board[i][j]){
                    numLive++;
                    button.setBackground(live);
                }
                else{
                    numDead++;
                    button.setBackground(dead);
                }
                gameOfLife.add(button);
            }
        }
        return gameOfLife;
    }

    /**
     * creates seed buttons that effect seed[][] when pressed. 
     * @returns JPanel with buttons in GridLayout s.t clicking a button inverses the value of the corresponding seed
     */
    private JPanel seedButtonsLayout() {
        JPanel seedButtons = new JPanel();
        seedButtons.setLayout(new GridLayout(height, width)); // Set grid layout
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                JButton button = new JButton(String.valueOf(i) + ", " + String.valueOf(j));
                button.setOpaque(true);
                button.setBorderPainted(false);
                if (seed[i][j]){
                    button.setBackground(live);
                }
                else{
                    button.setBackground(dead);
                }
                //When the button is clicked send i,j to the ActionListener
                button.setActionCommand(String.valueOf(i) + "," + String.valueOf(j));
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        JButton but = (JButton) ae.getSource();
                        String coord = but.getActionCommand(); //Get i,j
                        String[] coords = coord.split(","); //split into an array
                        int i = Integer.parseInt(coords[0]);
                        int j = Integer.parseInt(coords[1]);
                        seed[i][j] = !seed[i][j]; //set seed[i][j] to the opposite of what it was
                        //Update background color
                        if (seed[i][j]) {
                            button.setBackground(live);
                        }
                        else{
                            button.setBackground(dead);
                        }
                    }
                });
                seedButtons.add(button);
            }
        }
        return seedButtons;
    }

    /**
     * Creates Layout with the buttons for comonly used tasks (next tick, previous tick, and run set number of ticks)
     * @return JPanel layout with the buttons for UI control
     */
    private JPanel uiButtonsLayout() {
        JPanel ui = new JPanel();
        JButton prev = new JButton("<-");
        prev.addActionListener((ActionListener) new previousTickAction());
        JButton nxt = new JButton("->");
        nxt.addActionListener((ActionListener) new nextTickAction());
        JButton rn = new JButton("Run " + numTicks + " from start");
        rn.addActionListener((ActionListener) new runAction() );
        JButton reset = new JButton("Reset");
        reset.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                isStarted = false;
                startUI();
            }
        });
        JLabel aliveLabel = new JLabel("Alive: " + numLive);
        JLabel deadLabel = new JLabel("Dead: "+ numDead);
        JLabel tickNum = new JLabel("Tick: " + currentTick);
        ui.add(prev);
        ui.add(nxt);
        ui.add(reset);
        ui.add(rn);
        ui.add(aliveLabel);
        ui.add(deadLabel);
        ui.add(tickNum);
        return ui;
    }

    /**
     * Combines 2 JPanel's into 1 s.t the UI buttons are on top and have a limited height and the middle Panel takes up the rest of the space
     * @param middle main JPanel. should be SeedButtons or mainView
     * @param ui UI buttons panel to be put on top
     * @return JPanel with both panels combined
     */
    private JPanel combinedLayout(JPanel middle, JPanel ui){
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(ui);
        mainPanel.add(middle);
        ui.setMaximumSize(new Dimension(400, 3000));
        return mainPanel;
    }

    /**
     * loads settings.txt file and puts contents into variables
     * if some are unreadable, uses defaults
     */
    private void loadSettingsFile(){
        /*
        ORDER
        1) height
        2) width
        3) numTicks
        4) ouput prefix
        5) dead color
        6) live color */
        String file = "settings.txt";
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            int lineNumber = 0;
            String line;
            while ( (line=br.readLine()) != null ){
                if (lineNumber == 0){
                    try{
                        height = Integer.parseInt(line);
                    }catch(NumberFormatException e){
                        System.out.println("ERROR: Height value is not a number");
                        height = 5;
                    }
                }
                else if (lineNumber == 1){
                    try{
                        width = Integer.parseInt(line);
                    }catch(NumberFormatException e){
                        System.out.println("ERROR: Width value is not a number");
                        width = 5;
                    }
                }
                else if (lineNumber == 2){
                    try{
                        numTicks = Integer.parseInt(line);
                    }catch(NumberFormatException e){
                        System.out.println("ERROR: Number of Ticks must be a number");
                        numTicks = 10;
                    }
                }
                else if (lineNumber == 3){
                    outputPrefix = line;
                }
                else if (lineNumber == 4){
                    try{
                        Field field = Class.forName("java.awt.Color").getField(line);
                        dead = (Color) field.get(null);
                        deadColor = line;
                    }catch(Exception e){
                        System.out.println("ERROR: Could not read dead color");
                        dead = Color.GRAY;
                    }
                }
                else if (lineNumber == 5){
                    try{
                        Field field = Class.forName("java.awt.Color").getField(line);
                        live = (Color) field.get(null);
                        liveColor = line;
                    }catch (Exception e){
                        System.out.println("ERROR: could not read live color");
                        live = Color.YELLOW;
                    }
                }
                else{
                    System.out.println("ERROR: Settings file too long");
                }
                lineNumber++;
            }
        }catch (Exception e){
            System.out.println("ERROR: Settings file unreadable. Default settings will be used");
            height = 5;
            width = 5;
            numTicks = 10;
            outputPrefix = "output";
            dead = Color.GRAY;
            live = Color.YELLOW;
            saveConfigToFile();
        }
    }

    /**
     * Saves current variables to settings.txt
     */
    private void saveConfigToFile(){
        try{
            File settings = new File("settings.txt");
            FileWriter f = new FileWriter(settings, false);
            String output = height + "\n" + width + "\n" + numTicks + "\n" + outputPrefix + "\n" + deadColor + "\n" + liveColor;
            f.write(output);
            f.close();
        }catch(Exception e){
            System.out.println("ERROR: Saving settings file failed");
        }
    }
    
    //ACTION LISTENERS FOR ALL BUTTONS USED

    /**
     * opens file chooser to select seed file. Read's file into seed[][]
     */
    private class selectSeedActon implements ActionListener {
        public void actionPerformed(ActionEvent e){
            int pos = 0;
            JFileChooser j = new JFileChooser(new File(System.getProperty("user.dir"))); 
            String f = null;
            int r = j.showOpenDialog(null);
            if (r==JFileChooser.APPROVE_OPTION) {
                f = j.getSelectedFile().getAbsolutePath();
                BufferedReader br = null;
                try {
                        br = new BufferedReader(new FileReader(f));
                } catch (Exception ex){
                    ex.printStackTrace();
                }
                String line;
                int lineNumber = 0;
                try{
                    while ((line = br.readLine()) != null) {
                        if (lineNumber == 0){
                            String[] info = line.split(" ");
                            if (info.length == 2){
                                try{
                                    height = Integer.parseInt(info[0]);
                                    width = Integer.parseInt(info[1]);
                                    seed = new boolean[height][width];
                                    for (int i = 0; i < height; i++) {
                                        for (int k = 0; k < width; k++) {
                                            seed[i][k] = false;
                                        }
                                    }
                                } catch (Exception ex) {
                                    System.out.println("ERROR: Height or Width could not be read");
                                    ex.printStackTrace();
                                }
                            }
                            else {
                                System.out.println("ERROR: Seedfile's first line must be height, width");
                            }
                        }
                        else {
                            String[] cells = line.split(", ");
                            if (cells.length == width) {
                                for (int i=0; i<width; i++){
                                    int c = 0;
                                    try {
                                        c = Integer.parseInt(cells[i]);
                                    } catch (Exception ex){
                                        System.out.println("ERROR: Could not read seed file characters as integers");
                                        ex.printStackTrace();
                                    }
                                    seed[pos][i] = (c == 1);
                                }
                                pos++;
                            }
                            else {
                                System.out.println("ERROR: Seed file format incorrect. Each line must have " + width + " cells");
                            }
                        }
                        lineNumber++;
                    }
                    b = new Board(height, width, seed);
                    if (!isStarted){
                        remove(seedButtons);
                        isStarted = true;
                    }
                    else{
                        remove(mainView);
                    }
                    presentGameOfLife(true);
                }
                catch (Exception ex) {
                    System.out.println("ERROR: Seedfile unreadable");
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     *Save's current board into file titled based on the tick number and the output prefix. 
     */
    private class saveTickAction implements ActionListener { 
        public void actionPerformed(ActionEvent e) {
            String toPrint = b.printCurrentBoard();
            FileWriter fileWriter;
            try{
                fileWriter = new FileWriter(outputPrefix + "-" + currentTick + ".txt");
                fileWriter.write(toPrint);
                fileWriter.close();
            } catch (Exception ex){
                System.out.println("ERROR: Writing to file failed");
                ex.printStackTrace();
            }

        }
    }

    /**
     * If the gameOfLife has not been started, start the game
     * otherwise increments the board by one tick
     */
    private class nextTickAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (isStarted) {
                presentGameOfLife(false);
            }
            else {
                isStarted = true;
                b = new Board(height, width, seed);
                presentGameOfLife(true);
            }
        }
    }

    /**
     * Goes to the previous (non-seed) tick
     */
    private class previousTickAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e){
            if (isStarted){
                int tempTick = currentTick - 1;
                b = new Board(height, width, seed);
                currentTick = 0;
                while (currentTick < tempTick){
                    presentGameOfLife(false);
                }
            }
        }
    }


    /**
     * Runs GameOfLife for a set number of ticks with a short break between.
     * if the game has already been started, resets it.
     */
    private class runAction implements ActionListener {
            public void actionPerformed(ActionEvent e){
                if (isStarted){
                    remove(mainView);
                    currentTick = 0;
                }
                else{
                    remove(seedButtons);
                    isStarted = true;
                }
                b = new Board(height, width, seed);
                presentGameOfLife(true);
                    Timer timer = new Timer(1000, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (currentTick >= numTicks){
                            ((Timer)e.getSource()).stop();
                        }
                        presentGameOfLife(false);
                    }
                    });
                    timer.start();
            }
        }

    
    
    /**
     * Runs UI through EventQueue
     * @param args command line arguments. Nothing expected or handled
     */
    public static void main(String[] args){
        EventQueue.invokeLater( () -> {
            Gui g = new Gui();
            g.setVisible(true);
        });
    }
}
