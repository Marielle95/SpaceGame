/**
 * Copied from ASN3 - CS830 SPR22
 */

public class Position {

    private double x;
    private double y;

    private float angle = 0;
    private float speed = 0;
    
    public Position(){
    }

    //deep copy
    public Position(Position p){
        this.x = p.x;
        this.y = p.y;
        this.angle = p.angle;
        this.speed = p.speed;
    }
    
    //for initial position
    public Position(double startX, double startY){
        x = startX;
        y = startY;
    }
    
    public double x(){
        return x;
    }
    
    public double y(){
        return y;
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        if (this == p) return true;
        else if (this.x() == p.x() &&
                 this.y() == p.y()) return true;
        else return false;
    }

    //tests if Position check is within 0.1 distance of
    //Position goal. Goal is assumed to be current Position
    public boolean goalTest(Position check){
        //if (this.distance(check) <= 0.1) return true;

        if (check.y <= y) {
            //System.out.println("goal check true: " + check.y + " " + this.y);
            return true;      //for domain, only y position condition matters
        }
        else return false;
    }

    //uses distance formula to compute distance between another point and current
    double distance(Position p){
        double dx = p.x() - this.x();   //x2 - x1
        double dy = p.y() - this.y();   //y2 - y1
        double ax = dx * dx;            //square
        double ay = dy * dy;
        double result = Math.sqrt(ax + ay);
        //System.out.println("distance: " + result + " from " + this + " to " + p);
        return result;
    }

    //for debugging
    public String toString(){
        return x + "," + y;
    }
}