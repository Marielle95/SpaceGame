
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class Sprite extends Rectangle{
    
    public int posX;
    public int posY;
    public String id;

    Sprite(String id){
        super();
        this.id = id;
        switch (id){
            case "player":
                this.setFill(new ImagePattern(new Image("C:/Users/marie/Documents/cs830/project/GUI/SpaceGame/resources/ship.png")));
                break;
            case "asteroid":
                this.setFill(new ImagePattern(new Image("C:/Users/marie/Documents/cs830/project/GUI/SpaceGame/resources/asteroid1.jpg")));
            default:
                this.setFill(Color.TRANSPARENT);
        } 
    }

    Sprite(int x, int y, int w, int h, String id){
        super(w, h, Color.BLACK);
        this.posX = x;
        this.posY = y;
        this.id = id;
        setTranslateX(x);
        setTranslateY(y);

        switch (id){
            case "player":
                this.setFill(new ImagePattern(new Image("C:/Users/marie/Documents/cs830/project/GUI/SpaceGame/resources/ship.png")));
                break;
            case "asteroid":
                this.setFill(new ImagePattern(new Image("C:/Users/marie/Documents/cs830/project/GUI/SpaceGame/resources/asteroid2.png")));
        }
    }

    void moveRight(){
        setTranslateX(getTranslateX() + 10);
        posX += 10;
    }

    void moveLeft(){
        setTranslateX(getTranslateX() - 10);
        posX -= 10;
    }

    void moveDown(){
        setTranslateY(getTranslateY() + 10);
        posY += 10;
    }

    void moveUp(){
        setTranslateY(getTranslateY() - 10);
        posY -= 10;
    }

    void asteroidMove(){
        setTranslateX(posX + 5);
        posX += 5;
    }

    void printData(){
        String result = "\n";
        result += (this.posX - getWidth()/2) + ",";
        result += (this.posY - getHeight()/2) + " ";
        result += (this.posX + getWidth()/2) + ",";
        result += (this.posY - getHeight()/2) + " ";
        result += "\n";
        result += (this.posX - getWidth()/2) + ",";
        result += (this.posY + getHeight()/2) + " ";
        result += (this.posX + getWidth()/2) + ",";
        result += (this.posY + getHeight()/2) + " ";
        System.out.println(result); 
    }

    public String toString(){
        String result = "";
        result += posX + ", ";
        result += posY + " ";
        return result;
    }
}