import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.AffineTransform;

//import javax.sound.sampled.Line;

class Node {

    Node parent = null;
    Position p;
    Node(Position position){ 
        p = position;
    }

    void connect(Node n){
        n.parent = this;
    }

    //connect to current node
    //returns new node, now connected
    Node conect(Position pos){
        Node n = new Node(pos);
        n.parent = this;
        return n;
    }

    //return distance to this node from node n
    double distance(Node n){
        return p.distance(n.p);
    }

}

public class RRT {
    private List<Sprite> blocked;            //to check against
    private double x_minBound = 0.0d;
    private double x_maxBound;
    private double y_minBound = 0.0d;
    private double y_maxBound;

    private double width;      //bounding box 
    private double height;

    private Position start;
    private Position goal;

    //time-sensitive helper items
    private Node best;
    private long entryTime;
    private final int ALLOTTED = 80;
    private final double MIN_DIST = 10.0;

    private int SEED = 1200;
    Random rand = new Random(SEED);
    Random nextX = new Random(SEED);
    Random nextY = new Random(SEED);
    
    //solution related variables
        private ArrayList<Node> solution = new ArrayList<>();
        //private ArrayList<Node> motionTree = new ArrayList<>();

        
    //constructor
    public RRT(World world){
        x_maxBound = world.width;
        y_maxBound = world.height;
        width = world.player.getWidth();
        height = world.player.getHeight();
        blocked = (world.collisions);    //create new list based off collision objects detected at creation
        entryTime = System.currentTimeMillis();
    }

    public void solve(World world){
        blocked = new ArrayList<>(world.collisions);    //recreate list
        entryTime = System.currentTimeMillis();
        // if (solution.size() > 0){                    //testing purposes
        //     Node from = solution.get(0);
        //     for (int i = 1; i < solution.size(); i++){
        //         Node to = solution.get(i);
        //         if (!isValid(from, to)){
        //             start = new Position(world.getStart());
        //             goal = new Position(0.0d, world.goal);  //start and goal positions
        //             solution = new ArrayList<>();
        //             run();
        //         }
        //     }
        // }
        // else{
            start = new Position(world.getStart());
            goal = new Position(0.0d, world.goal);  //start and goal positions
            //solution.clear();
            solution = new ArrayList<>();
            run();
        //}
    }

    public void run(){
        ArrayList<Node> nodes = new ArrayList<>();

        Node root = new Node(start);
        Node n = root;
        best = root;

        //for(int i = 0; i < 60; i++){        //limit for debugging/testing
        for(;;){
            if (goal.goalTest(n.p)){
                //System.out.println("goal");
                //best = n;
                finish(n);
                return;
            }
            else if (timeout()){
                finish(best);
                return;
            }
            nodes.add(n);

            Node nearest = null;
            Node next = null;
            do {            //generate random states to look at. discard invalid/blocked states
                Position p;
                double chance = rand.nextDouble();
                if (chance <= 0.2) p = new Position(start.x(), goal.y());  //p = new Position(goal.x(), goal.y());   //goal bias
                else{
                    // double randX = Math.round(x_minBound + (x_maxBound - x_minBound) * rand.nextDouble());
                    // double randY = Math.round(y_minBound + (y_maxBound - y_minBound) * rand.nextDouble());
                    int index = (int)Math.round(rand.nextDouble() * (nodes.size()-1));
                    double randX = nodes.get(index).p.x();
                    double randY = nodes.get(index).p.y();
                    //double randX = n.p.x();
                    //double randY = n.p.y();
                    //if (chance <= 0.5) {
                        randX = Math.round(x_minBound + (x_maxBound - x_minBound) * nextX.nextDouble());
                    //}
                    //else {
                        randY = Math.round(y_minBound + (y_maxBound - y_minBound) * rand.nextDouble());
                    //}
                    p = new Position(randX, randY);
                }

                next = new Node(p);
                for (int j = 0; j < nodes.size(); j++){
                    if (nearest == null){
                            nearest = nodes.get(j);
                    }
                    else{
                        if (next.distance(nodes.get(j)) < next.distance(nearest)) {
                                nearest = nodes.get(j);
                        }
                    }
                }
                // if (timeout()){
                //     finish(best);
                //     return;
                // }
            } while (nearest == null);
            if (nearest.distance(next) < MIN_DIST) continue;
            //System.out.println("from " + nearest.p.x() + " " + next.p.x() + " to " + nearest.p.y() + " " + next.p.y());
            if (nearest.p.x() != next.p.x() && nearest.p.y() != next.p.y()){  //modification for 4 cardinal direction motion
                //System.out.println("cardinal direction modifier");
                Node interim = new Node(new Position(nearest.p.x(), next.p.y()));
                if (!isValid(nearest, interim) || !isValid(interim, next)){
                    interim = new Node(new Position(next.p.x(), nearest.p.y()));
                    if (!isValid(nearest, interim) || !isValid(interim, next)){
                        continue;   //invalid next to nearest
                    }
                }
                nodes.add(interim);
                //System.out.println("interim added " + interim.p.x() + " " + interim.p.y());
                nearest.connect(interim);
                interim.connect(next);
            }
            else{
                if (!isValid(nearest, next)) continue;
                nearest.connect(next);
            }
            n = next;
            if (next.p.distance(goal) < best.p.distance(goal)){
                best = next;
            }
        }
    }

    private boolean timeout(){
        if (Math.abs(entryTime - System.currentTimeMillis()) >= 200) return true;
        else return false;
    }

    private void finish(Node best){
        Node end = new Node(best.p);
        end.parent = best.parent;
        solve(end);
    }

    private void solve(Node goal){
        Node n = goal;
        while (n.parent != null){
            //System.out.println("solution size " + solution.size());
            solution.add(n);
            n = n.parent;
        }
        n.parent = n;
        solution.add(n);
        //printSolution();
    }

    //for collision checking with blocked states
    private boolean isValid(Node from, Node to){
        for (int i = 0; i < blocked.size(); i++){
            Sprite s = blocked.get(i);
            //Rectangle2D r = new Rectangle2D(s.posX, s.posY, s.getWidth(), s.getHeight());
            Rectangle2D r = new Rectangle2D.Double(s.posX, s.posY, s.getWidth(), s.getHeight());
            Line2D line = new Line2D.Double(from.p.x(), from.p.y(), to.p.x(), to.p.y());

            double w = line.getP1().distance(line.getP2());
            double l = line.getP1().distance(line.getP2());
            if (from.p.x() == to.p.x()) w = width;  //if vertical, length is sprite height
            else l = height;                          //else horizontal, width is sprite width
            double centerX = (from.p.x() + to.p.x()) / 2D;
            double centerY = (from.p.y() + to.p.y()) / 2D;
            double x1 = centerX - (w / 2D);
            double y1 = centerY - (l / 2D);
            Rectangle2D bound = new Rectangle2D.Double(x1, y1, centerX, centerY);

            if (r.intersects(bound)){
                //System.out.println("intersects");
                return false;
            }
        }
        return true;
        
    }

    public String results(){
        String result = "";
        for (Node n : solution){
            result += n.p.x() + "," + n.p.y() + " ";
        }
        return result;
    }

    public void printSolution(){
        System.out.println(solution.size());
        Collections.reverse(solution);
        for (Node n : solution){
            System.out.println(n.p);
        }
        /*
        System.out.println(motionTree.size());
        for (Node n : motionTree){
            System.out.println(n.p + " " + n.parent.p);
        }
        */
    }
    
}
