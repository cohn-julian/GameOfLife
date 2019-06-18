package csci4963.hw2;
import java.util.*;

public class Board {
    private boolean[][] board;
    private boolean[][] seed;
    int width, height;

    /**
     *
     * @param _height height of board
     * @param _width width of board
     * @param _seed seed true/false values for GameOfLife board
     */
    public Board(int _height, int _width, boolean[][] _seed){
        height = _height;
        width = _width;
        board = new boolean[height][width];
        seed = new boolean[height][width];
        for (int i=0; i<height; i++){
            board[i] = new boolean[width];
            for (int j=0; j<width; j++){
                boolean c = _seed[i][j];
                board[i][j] = c;
                seed[i][j] = c;
            }
        }
    }

    /**
     * 
     * get the seed 
     * @return the innitial seed this board started with
     */
    public boolean[][] getSeed(){
        return seed;
    }

    /**
     *
     * @param i, j position of Cell
     * @return number of live neighbors cell ij has
     */
    private int numLiveNeighbors(int i, int j) {
        int n = 0;
        // arrays of neighbors in i col and j col
        int[] di = new int[3];
        int[] dj = new int[3];
        di[0] = i;
        dj[0] = j;
        //check corner cases
        if (i + 1 == height) {
            di[1] = 0;
        } else {
            di[1] = i + 1;
        }
        if (j + 1 == width) {
            dj[1] = 0;
        } else {
            dj[1] = j + 1;
        }
        if (i == 0) {
            di[2] = height - 1;
        } else {
            di[2] = i - 1;
        }
        if (j == 0) {
            dj[2] = width - 1;
        } else {
            dj[2] = j - 1;
        }
        //for all neighbors, if it's alive increment count of live neighbors
        for (int x : di) {
            for (int y : dj) {
                if (!(x == i && y == j))
                    if (board[x][y]) n++;
            }
        }
        return n;
    }

    /**
     * updates board based on classic game of life rul
     * @return board as boolean grid
     */
    public boolean[][] tick(){
        boolean[][] board_new = new boolean[height][width];
        for (int i=0; i<height; i++){
            for (int j=0; j<width; j++){
                boolean c = board[i][j];
                board_new[i][j] = c;
                int numLiveNeighbors = numLiveNeighbors(i, j);
                if (c)
                    board_new[i][j] = !(numLiveNeighbors < 2 || numLiveNeighbors > 3);
                else
                    board_new[i][j] = numLiveNeighbors == 3;
            }
        }
        board = board_new;
        return board;
    }

    /**
     * @return current board
     */
    public String printCurrentBoard(){
        String ret = "";
        for (int i=0; i<height; i++){
            for (int j=0; j<width; j++){
                if (board[i][j])
                    ret += " 1 ";
                else
                    ret += " 0 ";
            }
            ret += "\n";
        }
        return ret;
    }

}
