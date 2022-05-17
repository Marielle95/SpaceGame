import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class Grid{

    int rows;
    int cols;
    Cell[][] grid;
    public Grid(int row, int col){
        rows = row;
        cols = col;
        init();
    }

    private void init(){
        grid = new Cell[rows][cols];
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                grid[i][j] = new Cell();
            }
        }
    }

    public boolean initToGridPane(GridPane gridpane){
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                //Label test = new Label("(" + i + "," + j + ")");
                gridpane.add(grid[i][j], i, j, 1, 1);
            }
        }
        gridpane.setAlignment(Pos.CENTER);
        return true;
    }

    //move object in cell to the right
    public void moveRight(int fromx, int fromy, int tox, int toy){
        if (fromx < 0 || fromx > rows){
            return;
        }
        else if (fromy < 0 || fromy > cols){
            return;
        }

        Cell from = grid[fromx][fromy];
        if (tox < 0 || tox > rows){
            //from
        }
    }
}