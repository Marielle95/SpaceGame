import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class Cell extends Rectangle{

    public Cell(){
        this.setWidth(20);
        this.setHeight(20);
        this.setFill(Color.TRANSPARENT);
        this.setStroke(Color.BLACK);
        //setImage(new Image())
    }

    public void setImage(Image n){
        ImagePattern ip = new ImagePattern(n);
        this.setFill(ip);
    }
}