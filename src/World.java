/**
 * Copied from ASN3 - CS830 SPR22
 * 
 * Holds information about the current state of the world
 * Continuous space with a list of collision objects.
 * 
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class World{
    
    int height;
    int width;

    Sprite player;

    double goal;

    List<Sprite> collisions = Collections.synchronizedList(new ArrayList<>());
        
    public World(int h, int w, Sprite player){
        height = h;
        width = w;
        this.player = player;
        goal = (double)0 + player.getHeight();
    }

    public synchronized void addAsteroid(Sprite a){
        collisions.add(a);
    }

    public synchronized void removeAsteroid(Sprite a){
        collisions.remove(a);
    }

    public Position getStart(){
        double x = player.posX;
        double y = player.posY;
        Position p = new Position(x, y);
        return p;
    }
}