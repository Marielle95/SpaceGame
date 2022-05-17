
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class App extends Application {

    final int SEED = 1500;
    Random r = new Random(SEED);

    final int WIDTH = 600;
    final int HEIGHT = 400;
    final int SHIPSIZE = 30;
    final int ASTEROIDSIZE = 30;
    final int BELT_BUFFER = 80;
    final double A_CHANCE = 0.15;
    final int GOAL = ASTEROIDSIZE;

    long startTime;
    long endTime;

    long asteroidMove;

    final int[] DIFFICULTY = {8, 12, 30};   //number of asteroids intially on field
    final int LEVEL = 2;
    private AtomicInteger asteroid_count = new AtomicInteger(0);

    private Pane root = new Pane();
    private Sprite player = new Sprite(WIDTH/2-(SHIPSIZE/2), HEIGHT-SHIPSIZE, SHIPSIZE, SHIPSIZE, "player");

    private Group asteroids = new Group();
    private Group trace = new Group();

    //private List<Sprite> asteroids = Collections.synchronizedCollection(new ArrayList<Sprite>());
    private List<KeyCode> keys = Collections.synchronizedList(new ArrayList<KeyCode>());
    private List<Line> lines = Collections.synchronizedList(new ArrayList<Line>());
    private KeyCode[] directions = {KeyCode.W, KeyCode.A, KeyCode.S, KeyCode.D, KeyCode.W};

    private World w;
    private Thread keyGen;
    private AtomicBoolean generating = new AtomicBoolean(true);
    private boolean playing = false;
    private RRT algorithm;

    private Parent createContent(){
        root.setPrefSize(WIDTH, HEIGHT);
        root.getChildren().add(trace);
        root.getChildren().add(player);
        root.getChildren().add(asteroids);
        asteroidMove = System.currentTimeMillis();

        w = new World(HEIGHT, WIDTH, player);
        algorithm = new RRT(w);
        initAsteroids();

        AnimationTimer animate = new AnimationTimer() {
            @Override
            public void handle(long nano){
                update();
                //try {Thread.sleep(100);} catch (Exception e){};
            }
        };
        animate.start();

        Platform.runLater(() ->{
            playGame();
        });

        //test keyboard automation!
        keyGen = new Thread(new Runnable() {
            @Override
            public void run() {
                long prevtime = System.currentTimeMillis();
                startTime = System.currentTimeMillis();
                while (generating.get()){
                    algorithm.solve(w);
                    System.out.println("return");
                    
                    //System.out.println(algorithm.results());
                    String [] points = algorithm.results().split("\\s");
                    
                    lines.clear();
                    double x = 0.0;
                    double y = 0.0;
                    for (int i = 0; i < points.length; i++){
                        String[] coord = points[i].split(",");
                        if (coord.length != 2){
                            System.out.println("ERROR: length " + coord.length);
                            for (String s : coord){
                                System.out.print(s + " ");
                            }
                            continue;
                            //System.exit(1);
                        }
                        if (i == 0){
                            x = Double.parseDouble(coord[0]);
                            y = Double.parseDouble(coord[1]);
                            continue;
                        }
                        double nx = Double.parseDouble(coord[0]);
                        double ny = Double.parseDouble(coord[1]);
                        Line l = new Line(x, y, nx, ny);
                        x = nx;
                        y = ny;
                        lines.add(l);
                    }
                    determineMove();

                    //try {Thread.sleep(100);} catch(Exception e){};
                    while (Math.abs(prevtime - System.currentTimeMillis()) < 50){
                        //wait
                    }
                    prevtime = System.currentTimeMillis();
                }
            }
        });
        keyGen.start();

        return root;
        //return grid;
    }

    public void initAsteroids(){
        Random initA = new Random(SEED);

        for (int i = 0; i < DIFFICULTY[LEVEL]; i++){
            int randX = Math.abs(initA.nextInt(WIDTH - ASTEROIDSIZE));
            int randY = BELT_BUFFER + (Math.abs(r.nextInt(HEIGHT-BELT_BUFFER-BELT_BUFFER)));
            Sprite s = new Sprite(randX, randY, ASTEROIDSIZE, ASTEROIDSIZE, "asteroid");
            asteroids.getChildren().add(s);
            w.addAsteroid(s);
            asteroid_count.getAndIncrement();
        }
    }

    public void determineMove(){
        if (lines.size() == 0) return;
        Line first = lines.get(lines.size()-1); //get last line since it builds backwards from goal
        // if (Math.abs(player.posX - first.getStartX()) < 5 && Math.abs(player.posY - first.getStartY()) < 5){
        //     if (lines.size() > 1){
        //         lines.remove(first);
        //         first = lines.get(lines.size()-1);  //remove line from solution if travelled
        //     }
        // }
        
        double fromx = first.getStartX();
        double fromy = first.getStartY();
        double tox = player.posX;
        double toy = player.posY;
        
        int lookup = 0;
        if (fromx < tox) lookup = 1;
        else if (fromx > tox) lookup = 3;
        else if (fromy > toy) lookup = 2;
        System.out.println("got: " + directions[lookup]);
        keys.add(directions[lookup]);
    }

    // public synchronized List<Sprite> sprites(){
    //     return root.getChildren().stream().map(n -> (Sprite)n).collect(Collectors.toList());
    // }

    public void update(){
        trace.getChildren().clear();
        trace.getChildren().addAll(lines);

        if (asteroid_count.getAcquire() < DIFFICULTY[LEVEL]){
            int randY = BELT_BUFFER + (Math.abs(r.nextInt(HEIGHT-BELT_BUFFER-BELT_BUFFER)));
            Sprite s = new Sprite((0 - ASTEROIDSIZE), randY, ASTEROIDSIZE, ASTEROIDSIZE, "asteroid");
            //int randX = r.nextInt(WIDTH-ASTEROIDSIZE);
            //Sprite s = new Sprite(randX, 0, ASTEROIDSIZE, ASTEROIDSIZE, "asteroid");
            asteroids.getChildren().add(s);
            w.addAsteroid(s);
            asteroid_count.getAndIncrement();
        }
        if (Math.abs(System.currentTimeMillis() - asteroidMove) >= 100){
            ArrayList<Sprite> removed = new ArrayList<>();
            asteroids.getChildren().forEach(n-> {
                Sprite s = (Sprite) n;
                switch (s.id){
                    case "asteroid":
                        s.asteroidMove();
                        if (s.posX - s.getWidth()/2 >= WIDTH) {
                            removed.add(s);
                            w.removeAsteroid(s);
                            asteroid_count.getAndDecrement();
                        }
                        break;
                }
            });
            asteroidMove = System.currentTimeMillis();
            removed.forEach(s -> asteroids.getChildren().remove(s));
        }
        if (player.posY <= GOAL){
            endgame();
            playing = false;
        }
    }

    public Parent mainmenu(){
        Pane screen = new Pane();
        screen.setPrefSize(WIDTH, HEIGHT);

        Button play = new Button("PLAY");
        Button robot = new Button("SOLVE");
        screen.getChildren().add(play);
        screen.getChildren().add(robot);

        play.setPrefHeight(50);
        play.setPrefWidth(200);
        play.setAlignment(Pos.CENTER);

        robot.setPrefHeight(50);
        robot.setPrefWidth(200);
        robot.setAlignment(Pos.CENTER);
        //play.set

        return screen;
    }

    public void loadGame(Stage stage) throws Exception{
        Scene scene = new Scene(createContent());
        scene.setOnKeyPressed(k -> {
            switch (k.getCode()){
                case A:
                    if (player.posX - 10 >= -1) player.moveLeft();
                    player.printData();
                    break;
                case D:
                if (player.posX + 10 <= WIDTH-SHIPSIZE) player.moveRight();
                    player.printData();
                    break;
                case W:
                if (player.posY - 10 >= -1) player.moveUp();
                    player.printData();
                    break;
                case S:
                if (player.posY + 10 <= HEIGHT-SHIPSIZE) player.moveDown();
                    //player.moveDown();
                    player.printData();
                    break;
            }
        });

        scene.setFill(new ImagePattern(new Image("C:/Users/marie/Documents/cs830/project/GUI/SpaceGame/resources/space1.jpg")));

        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                endgame();
            }
        });
    }

    public void endgame(){
        if (generating.get() || playing){
            endTime = System.currentTimeMillis();
            System.out.println("END: " + Math.abs(endTime-startTime));
        }
        generating.set(false);
    }

    public void playGame(){
        Robot rob = new Robot();
        
        AnimationTimer playgame = new AnimationTimer() {
            @Override
            public void handle(long nano){
                if (keys.size() > 0){
                    KeyCode k = keys.remove(0);
                    rob.keyPress(k);
                }
                if (player.posY <= GOAL) {
                    endgame();
                }
            }
        };
        playgame.start();
    }

    @Override
    public void start(Stage stage) throws Exception{

        //Scene first = new Scene(mainmenu());
        //first.setFill(new ImagePattern(new Image("C:/Users/marie/Documents/cs830/project/GUI/SpaceGame/resources/space1.jpg")));
        loadGame(stage);
        stage.getScene().setFill(new ImagePattern(new Image("C:/Users/marie/Documents/cs830/project/GUI/SpaceGame/resources/space1.jpg")));
    }

    public static void main(String[] args) {
        launch(args);
    }
}