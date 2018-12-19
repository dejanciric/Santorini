package com.example.cira.isproject.etf.santorini.cd150570d;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.GridLayout;
import android.widget.TextView;

import com.example.cira.isproject.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class ComputerVsComputerActivity extends AppCompatActivity {
    private static final int MY_REQUEST_PERMISSION_WRITE_EXTERNAL = 3;
    private static final int N = 5;
    private static final int PLUS_INF = 1000000;
    private static final int MINUS_INF = -1000000;

    private enum State{
        PLACING, SELECTING_MOVE, MOVING, BUILDING, GAME_OVER
    }
    private enum Player{
        RED, BLUE, NONE
    }

    // struct for AIMove that contains information about move, build, estimated heuristic function, alpha, beta..
    private class AIMove{
        private int xFigure, yFigure, xMove, yMove, xBuild, yBuild;
        private int score;
        private int alpha, beta;

        public AIMove(){

        }
        public AIMove(int sc){ score = sc;}


    }

    // struct that represent one field on board and information about it's state
    private class Field{
        private ComputerVsComputerActivity.Player playerOn;
        private int level;
        private boolean isGreen;
        public Field(){
            playerOn = ComputerVsComputerActivity.Player.NONE;
            level = 0;
            isGreen = false;
        }
    }

    private TextView[][] views = new TextView[N][N];
    private ComputerVsComputerActivity.Field[][] fields = new ComputerVsComputerActivity.Field[N][N];
    private ComputerVsComputerActivity.State currentState;
    private ComputerVsComputerActivity.Player turn;
    private int leftPlacing;
    private TextView currentMovingView;
    private ComputerVsComputerActivity.Field currentMovingField;
    private TextView gameInfo;
    private String line = "";
    private String difficulty;
    boolean readFile;
    boolean finish = false;
    private String numToLetter[] = {"A", "B", "C", "D", "E"};

    // initial method that execute on activity start
    // initialize some structures and check if read from file is checked
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_computer_vs_computer);
        turn = ComputerVsComputerActivity.Player.BLUE;
        leftPlacing = 2;
        currentState = ComputerVsComputerActivity.State.PLACING;
        gameInfo = findViewById(R.id.gameInfo);
        gameInfo.setText("BLUE placing, count: "+leftPlacing);
        GridLayout gridLayout = findViewById(R.id.gridLayout);

        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                fields[i][j] = new ComputerVsComputerActivity.Field();
                views[i][j] = (TextView)gridLayout.getChildAt(i*N+j);
            }
        clearOutputFile();

        Intent intent = getIntent();
        readFile = intent.getBooleanExtra("readFile",false);
        difficulty = intent.getStringExtra("difficulty");
        if (readFile)
            readFromInputFile();

    }

    // Main logic of game
    // execute code based on the current state of the game (PLACING, SELECTING_MOVE, MOVING, BUILDING, GAME_OVER)
    public void onFieldClick(View view) {

        String tag = (String) view.getTag();
        int x = getX(tag);
        int y = getY(tag);

        TextView selectedView = views[x][y];
        ComputerVsComputerActivity.Field selectedField = fields[x][y];


        switch (currentState){
            case PLACING:
                if (selectedField.playerOn == ComputerVsComputerActivity.Player.NONE){
                    if (turn == ComputerVsComputerActivity.Player.BLUE){
                        leftPlacing--;
                        line += numToLetter[x]+(y+1);
                        AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
                        animation1.setDuration(1000);

                        selectedView.startAnimation(animation1);
                        selectedView.setBackgroundColor(getBlueColor());
                        selectedField.playerOn = ComputerVsComputerActivity.Player.BLUE;

                        if (leftPlacing == 0){
                            leftPlacing = 2;
                            turn = ComputerVsComputerActivity.Player.RED;
                            gameInfo.setText("RED placing, count: "+leftPlacing);
                            writeTurnInOutputFile(line);
                            line="";



                        }else{
                            gameInfo.setText("BLUE placing, count: "+leftPlacing);
                            line+=" ";
                            if (!readFile){
                                Random r = new Random();
                                int AIx = r.nextInt(N);
                                int AIy = r.nextInt(N);
                                while (fields[AIx][AIy].playerOn == Player.BLUE || fields[AIx][AIy].playerOn == Player.RED ){
                                    AIx = r.nextInt(N);
                                    AIy = r.nextInt(N);
                                }
                                onFieldClick(views[AIx][AIy]);
                                return;
                            }
                        }
                    }else{
                        leftPlacing--;
                        line += numToLetter[x]+(y+1);
                        AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
                        animation1.setDuration(1000);

                        selectedView.startAnimation(animation1);


                        selectedView.setBackgroundColor(getRedColor());
                        selectedField.playerOn = ComputerVsComputerActivity.Player.RED;
                        if (leftPlacing == 0){
                            turn = ComputerVsComputerActivity.Player.BLUE;
                            currentState = ComputerVsComputerActivity.State.SELECTING_MOVE;
                            gameInfo.setText("BLUE turn: ");
                            writeTurnInOutputFile(line);
                            line="";
                        }else{
                            gameInfo.setText("RED placing, count: "+leftPlacing);
                            line+=" ";
                            if (!readFile){
                                Random r = new Random();
                                int AIx = r.nextInt(N);
                                int AIy = r.nextInt(N);
                                while (fields[AIx][AIy].playerOn == Player.BLUE || fields[AIx][AIy].playerOn == Player.RED ){
                                    AIx = r.nextInt(N);
                                    AIy = r.nextInt(N);
                                }
                                onFieldClick(views[AIx][AIy]);
                                return;
                            }

                        }
                    }

                }

                break;
            case SELECTING_MOVE:
                if (turn == ComputerVsComputerActivity.Player.BLUE && selectedField.playerOn == ComputerVsComputerActivity.Player.BLUE){
                    colorPossibleMoves(x, y);
                    if (!chechIfCanMove()){
                        currentState = ComputerVsComputerActivity.State.SELECTING_MOVE;
                    }else{
                        gameInfo.setText("BLUE move: ");
                        currentMovingField = selectedField;
                        currentMovingView = selectedView;
                        currentState = ComputerVsComputerActivity.State.MOVING;
                        line+=numToLetter[x]+(y+1);
                    }

                }else if(turn == ComputerVsComputerActivity.Player.RED && selectedField.playerOn == ComputerVsComputerActivity.Player.RED){
                    colorPossibleMoves(x, y);
                    if (!chechIfCanMove()){
                        currentState = ComputerVsComputerActivity.State.SELECTING_MOVE;
                    }else{
                        gameInfo.setText("RED move: ");
                        currentMovingField = selectedField;
                        currentMovingView = selectedView;
                        currentState = ComputerVsComputerActivity.State.MOVING;
                        line+=numToLetter[x]+(y+1);
                    }

                }

                break;
            case MOVING:
                if (selectedField.isGreen){
                    currentMovingField.playerOn = ComputerVsComputerActivity.Player.NONE;
                    AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
                    animation1.setDuration(1000);

                    selectedView.startAnimation(animation1);
                    currentMovingView.setBackgroundColor(getDefaultColor());
                    clearGreen();
                    selectedField.playerOn = turn;
                    if (selectedField.level == 3){
                        if (turn == ComputerVsComputerActivity.Player.BLUE){
                             animation1 = new AlphaAnimation(0.2f, 1.0f);
                            animation1.setDuration(1000);

                            selectedView.startAnimation(animation1);
                            selectedView.setBackgroundColor(getBlueColor());
                            gameInfo.setText("BLUE won!");
                        }else{
                             animation1 = new AlphaAnimation(0.2f, 1.0f);
                            animation1.setDuration(1000);

                            selectedView.startAnimation(animation1);
                            selectedView.setBackgroundColor(getRedColor());
                            gameInfo.setText("RED won!");
                        }
                        line+= " "+numToLetter[x]+(y+1);
                        writeTurnInOutputFile(line);
                        currentState = ComputerVsComputerActivity.State.GAME_OVER;
                    }else{
                        if (turn == ComputerVsComputerActivity.Player.BLUE){
                            gameInfo.setText("BLUE build: ");
                             animation1 = new AlphaAnimation(0.2f, 1.0f);
                            animation1.setDuration(1000);

                            selectedView.startAnimation(animation1);
                            selectedView.setBackgroundColor(getBlueColor());
                        }

                        else{
                            gameInfo.setText("RED build: ");
                             animation1 = new AlphaAnimation(0.2f, 1.0f);
                            animation1.setDuration(1000);

                            selectedView.startAnimation(animation1);
                            selectedView.setBackgroundColor(getRedColor());
                        }
                        line+= " "+numToLetter[x]+(y+1);

                        colorPossibleBuilds(x,y);
                        currentState = ComputerVsComputerActivity.State.BUILDING;
                    }

                }

                break;

            case BUILDING:
                if (selectedField.isGreen){

                    clearGreen();
                    selectedField.level++;
                    AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
                    animation1.setDuration(1000);

                    selectedView.startAnimation(animation1);
                    selectedView.setText(selectedField.level+"");
                    if (turn == ComputerVsComputerActivity.Player.BLUE){
                        gameInfo.setText("RED turn: ");
                        turn = ComputerVsComputerActivity.Player.RED;
                        line+= " "+numToLetter[x]+(y+1);
                        writeTurnInOutputFile(line);
                        line = "";



                    }

                    else{
                        gameInfo.setText("BLUE turn: ");
                        turn = ComputerVsComputerActivity.Player.BLUE;
                        line+= " "+numToLetter[x]+(y+1);
                        writeTurnInOutputFile(line);
                        line = "";
                    }

                    if (currentState != State.GAME_OVER)
                        currentState = ComputerVsComputerActivity.State.SELECTING_MOVE;
                    checkIsGameOver();
                }

                break;
        }


    }

    // Recursive method that implement minimax algorithm with alpha beta pruning
    // and return best move for computer
    private AIMove getBestMoveAlphaBeta(Player player, int h, AIMove moveForHeuristic, int alpha, int beta) {
            int alpha1 = alpha;
            int beta1 = beta;
        // add or game finished
        if (h == 2)
            return new AIMove(heuristicFunction(moveForHeuristic, Player.BLUE));
      /*  else if (moveForHeuristic != null &&(isGameOver(player) || fields[moveForHeuristic.xMove][moveForHeuristic.yMove].level == 3)){
            return new AIMove(heuristicFunction(moveForHeuristic, Player.BLUE));
        }*/

        ArrayList<AIMove> oneNodeMoves = new ArrayList<AIMove>();

        ArrayList<AIMove> moves = getAllPossibleMovesAndBuilds(player);
        for (int i = 0; i < moves.size(); i++){
            AIMove move = moves.get(i);
            doMove(move, player);
            if (player == Player.RED){
                move.score = getBestMoveAlphaBeta(Player.BLUE, ++h, move, alpha1, beta1).score;
            }else{
                move.score = getBestMoveAlphaBeta(Player.RED, ++h, move, alpha1, beta1).score;
            }
            h--;
            if (h == 1){
                if (move.score > alpha1){
                    move.alpha = move.score;
                    alpha1 = move.score;
                }

            }else if (h == 0){
                if (beta1 > move.alpha){
                    move.beta = move.alpha;
                    beta1 = move.alpha;
                }

            }
            oneNodeMoves.add(move);
            undoMove(move, player);
            if (alpha1 >= beta1)
                break;
        }

        if (oneNodeMoves.size() == 0){
            if (player == Player.RED){
                return new AIMove(100000);
            }else{
                return new AIMove(-100000);
            }

        }
        int bestMoveIndex = 0;
        if (player == Player.RED){
            int min = PLUS_INF;
            for (int i = 0; i < oneNodeMoves.size(); i++)
                if (oneNodeMoves.get(i).score < min){
                    min = oneNodeMoves.get(i).score;
                    bestMoveIndex = i;
                }

        }else{
            int max = MINUS_INF;
            for (int i = 0; i < oneNodeMoves.size(); i++)
                if (oneNodeMoves.get(i).score > max){
                    max = oneNodeMoves.get(i).score;
                    bestMoveIndex = i;
                }
        }
        if (moveForHeuristic != null && h == 1){
            if (player == Player.RED){
                undoMove(moveForHeuristic, Player.BLUE);
                oneNodeMoves.get(bestMoveIndex).score -= heuristicFunction(moveForHeuristic, Player.BLUE);
                doMove(moveForHeuristic, Player.BLUE);
            }else{
                undoMove(moveForHeuristic, Player.RED);
                oneNodeMoves.get(bestMoveIndex).score -= heuristicFunction(moveForHeuristic, Player.RED);
                doMove(moveForHeuristic, Player.RED);
            }

        }

        return oneNodeMoves.get(bestMoveIndex);
    }

    // Method that perform passed move ( used when computer find best move from minimax )
    private void performMove(AIMove bestMove, Player red) {
        currentState = State.SELECTING_MOVE;
        onFieldClick(views[bestMove.xFigure][bestMove.yFigure]);
        onFieldClick(views[bestMove.xMove][bestMove.yMove]);
        onFieldClick(views[bestMove.xBuild][bestMove.yBuild]);
        //currentState = State.BUILDING;

    }

    // Recursive method that implement minimax algorithm and return best move for computer
    private AIMove getBestMove(Player player, int h, AIMove moveForHeuristic){

        // add or game finished
        if (h == 2){
            if (player == Player.RED){
                return new AIMove(heuristicFunction(moveForHeuristic, Player.BLUE));
            }else{
                return new AIMove(heuristicFunction(moveForHeuristic, Player.RED));
            }
        }

      /*  else if (moveForHeuristic != null &&(isGameOver(player) || fields[moveForHeuristic.xMove][moveForHeuristic.yMove].level == 3)){
            return new AIMove(heuristicFunction(moveForHeuristic, Player.BLUE));
        }*/

        ArrayList<AIMove> oneNodeMoves = new ArrayList<AIMove>();

        ArrayList<AIMove> moves = getAllPossibleMovesAndBuilds(player);
        for (int i = 0; i < moves.size(); i++){
            AIMove move = moves.get(i);
            doMove(move, player);
            if (player == Player.RED){
                move.score = getBestMove(Player.BLUE, h+1, move).score;
            }else{
                move.score = getBestMove(Player.RED, h+1, move).score;
            }

            oneNodeMoves.add(move);
            undoMove(move, player);
        }

        if (oneNodeMoves.size() == 0){
            if (player == Player.RED){
                return new AIMove(100000);
            }else{
                return new AIMove(-100000);
            }
        }

        int bestMoveIndex = 0;
        if (player == Player.RED){
            int min = 1000000;
            for (int i = 0; i < oneNodeMoves.size(); i++)
                if (oneNodeMoves.get(i).score < min){
                    min = oneNodeMoves.get(i).score;
                    bestMoveIndex = i;
                }

        }else{
            int max = -1000000;
            for (int i = 0; i < oneNodeMoves.size(); i++)
                if (oneNodeMoves.get(i).score > max){
                    max = oneNodeMoves.get(i).score;
                    bestMoveIndex = i;
                }
        }
        if (moveForHeuristic != null && h == 1){
            if (player == Player.RED){
                undoMove(moveForHeuristic, Player.BLUE);
                oneNodeMoves.get(bestMoveIndex).score -= heuristicFunction(moveForHeuristic, Player.BLUE);
                doMove(moveForHeuristic, Player.BLUE);
            }else{
                undoMove(moveForHeuristic, Player.RED);
                oneNodeMoves.get(bestMoveIndex).score -= heuristicFunction(moveForHeuristic, Player.RED);
                doMove(moveForHeuristic, Player.RED);
            }

        }


        return oneNodeMoves.get(bestMoveIndex);

    }

    // Heuristic function that calculate and return estimated value
    private int heuristicFunction(AIMove move, Player player) {
        if (difficulty.equals("Hard")){
            if (player == Player.BLUE){

                if (fields[move.xMove][move.yMove].level == 3){
                    return PLUS_INF+100000;
                }
                if (fields[move.xBuild][move.yBuild].level == 3 && isDistanceOne(move.xBuild, move.yBuild, Player.RED)){
                    return PLUS_INF;
                }
                int score = fields[move.xMove][move.yMove].level + fields[move.xBuild][move.yBuild].level*distance(move.xBuild, move.yBuild, player);
                return score;

            }else{
                //ovo znaci da je red i guram min
                if (fields[move.xMove][move.yMove].level == 3){
                    return PLUS_INF+100000;
                }
                if (fields[move.xBuild][move.yBuild].level == 3 && isDistanceOne(move.xBuild, move.yBuild, Player.BLUE)){
                    return PLUS_INF;
                }
                int score = fields[move.xMove][move.yMove].level + fields[move.xBuild][move.yBuild].level*distance(move.xBuild, move.yBuild, player);
                return score;
            }
        }else{
            int score = fields[move.xMove][move.yMove].level + fields[move.xBuild][move.yBuild].level*distance(move.xBuild, move.yBuild, player);
            return score;
        }

    }

    // calculate subtraction of 2 players figures distance
    private int distance(int xBuild, int yBuild, Player player) {
        int dist1 = calcDistanceForPlayer(xBuild, yBuild, player);
        int dist2 = 0;
        if (player == Player.RED){
            dist2 = calcDistanceForPlayer(xBuild, yBuild, Player.BLUE);
        }else{
            dist2 = calcDistanceForPlayer(xBuild, yBuild, Player.RED);
        }
        return dist1-dist2;
    }

    // calculate distance for one player, sum of both figures distances
    private int calcDistanceForPlayer(int xBuild, int yBuild, Player player) {
        ArrayList<Integer> dists = new ArrayList<Integer>();
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                if (fields[i][j].playerOn == player)
                    dists.add(calcDistanceForFigure(xBuild, yBuild, i, j));
            }
        int distance = dists.get(0)+dists.get(1);
        return distance;
    }

    // check if player is next to passed field
    private boolean isDistanceOne(int xBuild, int yBuild, Player player){
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                if (fields[i][j].playerOn == player)
                    if (calcDistanceForFigure(xBuild, yBuild, i, j) == 1)
                        return true;
            }

        return false;
    }

    // calculate distance for one given figure
    private Integer calcDistanceForFigure(int dstX, int dstY, int figX, int figY) {
        int figureX = figX;
        int figureY = figY;
        int distance = 0;
        while (dstX < figureX && dstY < figureY){
            figureX--; figureY--;
            distance++;
        }
        while (dstX < figureX && dstY > figureY){
            figureX--; figureY++;
            distance++;
        }
        while (dstX > figureX && dstY < figureY){
            figureX++; figureY--;
            distance++;
        }
        while (dstX > figureX && dstY > figureY){
            figureX++; figureY++;
            distance++;
        }
        while (dstX < figureX && dstY == figureY){
            figureX--;
            distance++;
        }
        while (dstX == figureX && dstY > figureY){
            figureY++;
            distance++;
        }
        while (dstX > figureX && dstY == figureY){
            figureX++;
            distance++;
        }
        while (dstX == figureX && dstY < figureY){
            figureY--;
            distance++;
        }
        return distance;
    }

    // do a move in case of recursively calculating in minimax algorithm for proper state
    private void doMove(AIMove move, Player player) {
        fields[move.xFigure][move.yFigure].playerOn = Player.NONE;
        fields[move.xMove][move.yMove].playerOn = player;
        fields[move.xBuild][move.yBuild].level++;
    }

    // undo a move in case of recursively calculating in minimax algorithm for proper state
    private void undoMove(AIMove move, Player player) {
        fields[move.xFigure][move.yFigure].playerOn = player;
        fields[move.xMove][move.yMove].playerOn = Player.NONE;
        fields[move.xBuild][move.yBuild].level--;

    }

    // get all potential moves and builds and return them as arraylist
    private ArrayList<AIMove> getAllPossibleMovesAndBuilds(Player player) {

        ArrayList<AIMove> moves = new ArrayList<AIMove>();
        // getting positions of all figures
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                if (fields[i][j].playerOn == player){
                    AIMove am = new AIMove();
                    am.xFigure = i;
                    am.yFigure = j;
                    moves.add(am);
                }

            }
        // for all figures get possible moves
        int n = moves.size();
        for (int y = 0; y < n; y++ ){
            colorPossibleMovesLogical(moves.get(y).xFigure, moves.get(y).yFigure);
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++){
                    if (fields[i][j].isGreen){
                        AIMove am = new AIMove();
                        am.xFigure = moves.get(y).xFigure;
                        am.yFigure = moves.get(y).yFigure;
                        am.xMove = i;
                        am.yMove = j;
                        moves.add(am);
                    }
                }
            clearGreenLogical();
        }
        //remove first 2 moves that contain only figure coordinates
        for (int y = 0; y < n; y++ )
            moves.remove(0);

        // for all possible moves for all figures get all possible builds
        n = moves.size();
        for (int y = 0; y < n; y++ ){
            // need to temporary move for possible builds calculations
            fields[moves.get(y).xFigure][moves.get(y).yFigure].playerOn = Player.NONE;
            fields[moves.get(y).xMove][moves.get(y).yMove].playerOn = player;

            colorPossibleBuildsLogical(moves.get(y).xMove, moves.get(y).yMove);

            // after calculate possibilities return as it was
            fields[moves.get(y).xFigure][moves.get(y).yFigure].playerOn = player;
            fields[moves.get(y).xMove][moves.get(y).yMove].playerOn = Player.NONE;

            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++){
                    if (fields[i][j].isGreen){
                        AIMove am = new AIMove();
                        am.xFigure = moves.get(y).xFigure;
                        am.yFigure = moves.get(y).yFigure;
                        am.xMove = moves.get(y).xMove;
                        am.yMove = moves.get(y).yMove;
                        am.xBuild= i;
                        am.yBuild= j;
                        moves.add(am);
                    }
                }
            clearGreenLogical();
        }
        //remove first n moves that contain only figure and move coordinates
        for (int y = 0; y < n; y++ )
            moves.remove(0);
        // now moves contain all possible moves and builds for all figures
        return moves;
    }

    // check if the game is over (someone won)
    private void checkIsGameOver() {
        if (turn == ComputerVsComputerActivity.Player.BLUE){
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++){
                    if (fields[i][j].playerOn == ComputerVsComputerActivity.Player.BLUE){
                        colorPossibleMoves(i,j);
                    }
                }

            if (!chechIfCanMove()){
                gameInfo.setText("RED won! BLUE can't move.");

                currentState = ComputerVsComputerActivity.State.GAME_OVER;
            }
            clearGreen();

        }else{
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++){
                    if (fields[i][j].playerOn == ComputerVsComputerActivity.Player.RED){
                        colorPossibleMoves(i,j);
                    }
                }

            if (!chechIfCanMove()){
                gameInfo.setText("BLUE won! RED can't move.");

                currentState = ComputerVsComputerActivity.State.GAME_OVER;
            }
            clearGreen();

        }

    }

    // check if player can even move
    private boolean chechIfCanMove() {
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                if (fields[i][j].isGreen){
                    return true;
                }
            }
        return false;
    }

    // green color all possible builds for moved figure
    private void colorPossibleBuilds(int x, int y) {
        if (x-1 >= 0 && fields[x-1][y].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x-1][y].level < 4){
            fields[x-1][y].isGreen = true;
            views[x-1][y].setBackgroundColor(getGreenColor());
        }
        if (x+1 < N && fields[x+1][y].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x+1][y].level < 4){
            fields[x+1][y].isGreen = true;
            views[x+1][y].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && fields[x][y-1].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x][y-1].level < 4){
            fields[x][y-1].isGreen = true;
            views[x][y-1].setBackgroundColor(getGreenColor());
        }
        if (y+1 < N && fields[x][y+1].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x][y+1].level < 4){
            fields[x][y+1].isGreen = true;
            views[x][y+1].setBackgroundColor(getGreenColor());
        }

        if (y+1 < N && x+1 < N && fields[x+1][y+1].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x+1][y+1].level < 4){
            fields[x+1][y+1].isGreen = true;
            views[x+1][y+1].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && x-1 >= 0 && fields[x-1][y-1].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x-1][y-1].level < 4){
            fields[x-1][y-1].isGreen = true;
            views[x-1][y-1].setBackgroundColor(getGreenColor());
        }
        if (y+1 < N && x-1 >= 0 && fields[x-1][y+1].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x-1][y+1].level < 4){
            fields[x-1][y+1].isGreen = true;
            views[x-1][y+1].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && x+1 < N && fields[x+1][y-1].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x+1][y-1].level < 4){
            fields[x+1][y-1].isGreen = true;
            views[x+1][y-1].setBackgroundColor(getGreenColor());
        }
    }

    // green color all possible builds for moved figure but only in stucture not in gui
    private void colorPossibleBuildsLogical(int x, int y) {
        if (x-1 >= 0 && fields[x-1][y].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x-1][y].level < 4){
            fields[x-1][y].isGreen = true;
        }
        if (x+1 < N && fields[x+1][y].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x+1][y].level < 4){
            fields[x+1][y].isGreen = true;
        }
        if (y-1 >= 0 && fields[x][y-1].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x][y-1].level < 4){
            fields[x][y-1].isGreen = true;
        }
        if (y+1 < N && fields[x][y+1].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x][y+1].level < 4){
            fields[x][y+1].isGreen = true;
        }

        if (y+1 < N && x+1 < N && fields[x+1][y+1].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x+1][y+1].level < 4){
            fields[x+1][y+1].isGreen = true;
        }
        if (y-1 >= 0 && x-1 >= 0 && fields[x-1][y-1].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x-1][y-1].level < 4){
            fields[x-1][y-1].isGreen = true;
        }
        if (y+1 < N && x-1 >= 0 && fields[x-1][y+1].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x-1][y+1].level < 4){
            fields[x-1][y+1].isGreen = true;
        }
        if (y-1 >= 0 && x+1 < N && fields[x+1][y-1].playerOn == ComputerVsComputerActivity.Player.NONE && fields[x+1][y-1].level < 4){
            fields[x+1][y-1].isGreen = true;
        }
    }

    // default color all fields that are green
    private void clearGreen() {
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                if (fields[i][j].isGreen){
                    fields[i][j].isGreen = false;
                    views[i][j].setBackgroundColor(getDefaultColor());
                }
            }
    }

    // default color all fields that was green but only in stucture not in gui
    private void clearGreenLogical() {
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                if (fields[i][j].isGreen){
                    fields[i][j].isGreen = false;
                }
            }
    }

    // green color all possible moves for selected figure but only in stucture not in gui
    private void colorPossibleMoves(int x, int y) {

        if (x-1 >= 0 && fields[x-1][y].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x-1,y) <= 1 && fields[x-1][y].level < 4){
            fields[x-1][y].isGreen = true;
            views[x-1][y].setBackgroundColor(getGreenColor());
        }
        if (x+1 < N && fields[x+1][y].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x+1,y) <= 1 && fields[x+1][y].level < 4){
            fields[x+1][y].isGreen = true;
            views[x+1][y].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && fields[x][y-1].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x,y-1) <= 1 && fields[x][y-1].level < 4){
            fields[x][y-1].isGreen = true;
            views[x][y-1].setBackgroundColor(getGreenColor());
        }
        if (y+1 < N && fields[x][y+1].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x,y+1) <= 1 && fields[x][y+1].level < 4){
            fields[x][y+1].isGreen = true;
            views[x][y+1].setBackgroundColor(getGreenColor());
        }

        if (y+1 < N && x+1 < N && fields[x+1][y+1].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x+1,y+1) <= 1 && fields[x+1][y+1].level < 4){
            fields[x+1][y+1].isGreen = true;
            views[x+1][y+1].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && x-1 >= 0 && fields[x-1][y-1].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x-1,y-1) <= 1 && fields[x-1][y-1].level < 4){
            fields[x-1][y-1].isGreen = true;
            views[x-1][y-1].setBackgroundColor(getGreenColor());
        }
        if (y+1 < N && x-1 >= 0 && fields[x-1][y+1].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x-1,y+1) <= 1 && fields[x-1][y+1].level < 4){
            fields[x-1][y+1].isGreen = true;
            views[x-1][y+1].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && x+1 < N && fields[x+1][y-1].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x+1,y-1) <= 1 && fields[x+1][y-1].level < 4){
            fields[x+1][y-1].isGreen = true;
            views[x+1][y-1].setBackgroundColor(getGreenColor());
        }
    }

    // green color all possible moves for selected figure but only in stucture not in gui
    private void colorPossibleMovesLogical(int x, int y) {

        if (x-1 >= 0 && fields[x-1][y].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x-1,y) <= 1 && fields[x-1][y].level < 4){
            fields[x-1][y].isGreen = true;

        }
        if (x+1 < N && fields[x+1][y].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x+1,y) <= 1 && fields[x+1][y].level < 4){
            fields[x+1][y].isGreen = true;

        }
        if (y-1 >= 0 && fields[x][y-1].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x,y-1) <= 1 && fields[x][y-1].level < 4){
            fields[x][y-1].isGreen = true;

        }
        if (y+1 < N && fields[x][y+1].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x,y+1) <= 1 && fields[x][y+1].level < 4){
            fields[x][y+1].isGreen = true;

        }

        if (y+1 < N && x+1 < N && fields[x+1][y+1].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x+1,y+1) <= 1 && fields[x+1][y+1].level < 4){
            fields[x+1][y+1].isGreen = true;
        }
        if (y-1 >= 0 && x-1 >= 0 && fields[x-1][y-1].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x-1,y-1) <= 1 && fields[x-1][y-1].level < 4){
            fields[x-1][y-1].isGreen = true;
        }
        if (y+1 < N && x-1 >= 0 && fields[x-1][y+1].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x-1,y+1) <= 1 && fields[x-1][y+1].level < 4){
            fields[x-1][y+1].isGreen = true;
        }
        if (y-1 >= 0 && x+1 < N && fields[x+1][y-1].playerOn == ComputerVsComputerActivity.Player.NONE && difference(x,y,x+1,y-1) <= 1 && fields[x+1][y-1].level < 4){
            fields[x+1][y-1].isGreen = true;
        }
    }

    // return difference in levels of two passed fields
    private int difference(int srcX, int srcY, int dstX, int dstY) {
        return fields[dstX][dstY].level - fields[srcX][srcY].level;
    }

    // return X coordinate from passed tag of field
    int getX(String tag){
        int x = Integer.parseInt(tag);
        x = x / N;
        return x;
    }

    // return Y coordinate from passed tag of field
    int getY(String tag){
        int y = Integer.parseInt(tag);
        y = y % N;
        return y;
    }

    // write just performed turn (line) in output.txt
    public void writeTurnInOutputFile(String line) {
        try {
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
            File outputFile = new File(storageDir, "output.txt");
            FileWriter writer = new FileWriter(outputFile, true);
            line+='\n';
            writer.append(line);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // make sure output.txt is empty on start of game
    public void clearOutputFile() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_REQUEST_PERMISSION_WRITE_EXTERNAL);

            } else {
                try {
                    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
                    File outputFile = new File(storageDir, "output.txt");
                    FileWriter writer = new FileWriter(outputFile);
                    writer.write("");
                    writer.flush();
                    writer.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        }
    }

    // read from input.txt
    void readFromInputFile(){
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
        File outputFile = new File(storageDir, "input.txt");
        try{

            FileReader reader = new FileReader(outputFile);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();
            while (line != null && line != ""){
                String coordinates[] = line.split(" ");
                for (int i = 0; i < coordinates.length; i++){
                    int x = letterToNum(coordinates[i].charAt(0)+"");
                    int y = numFromFile(coordinates[i].charAt(1)+"");
                    onFieldClick(views[x][y]);
                }
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            reader.close();
            readFile = false;

        }catch (Exception e){

        }
    }

    // parse num from file and return it as correct value for structures
    private int numFromFile(String s) {
        int y = Integer.parseInt(s);
        return y-1;
    }

    // return corresponding number from letters from file
    int letterToNum(String letter){

        for (int i = 0; i < N; i++)
            if (numToLetter[i].equals(letter))
                return i;

        return -1;
    }

    // perform move for a player that is on turn
    public void nextMove(View view) {
        if (currentState != State.GAME_OVER){
            if (currentState == State.PLACING){
                if (!readFile){
                    Random r = new Random();
                    int AIx = r.nextInt(N);
                    int AIy = r.nextInt(N);
                    while (fields[AIx][AIy].playerOn == Player.BLUE || fields[AIx][AIy].playerOn == Player.RED ){
                        AIx = r.nextInt(N);
                        AIy = r.nextInt(N);
                    }
                    onFieldClick(views[AIx][AIy]);

                }
            }else{
                if (!readFile){
                    if (turn == Player.RED){
                        if (difficulty.equals("Easy")){
                            performMove(getBestMove(Player.RED, 0, null), Player.RED);
                        }else if (difficulty.equals("Medium")){
                            performMove(getBestMoveAlphaBeta(Player.RED, 0, null, MINUS_INF, PLUS_INF), Player.RED);
                        }else{
                            performMove(getBestMove(Player.RED, 0, null), Player.RED);
                        }
                    }else{
                        if (difficulty.equals("Easy")){
                            performMove(getBestMove(Player.BLUE, 0, null), Player.BLUE);
                        }else if (difficulty.equals("Medium")){
                            performMove(getBestMoveAlphaBeta(Player.BLUE, 0, null, MINUS_INF, PLUS_INF), Player.BLUE);
                        }else{
                            performMove(getBestMove(Player.BLUE, 0, null), Player.BLUE);
                        }
                    }
                }

            }
        }

    }

    // do next move until state is game over, execute moves till someone win
    public void finish(View view) {
        finish = true;
        while(currentState != State.GAME_OVER){
            nextMove(null);

        }

    }

    // return value for default color of field
    private int getDefaultColor() {
        return ContextCompat.getColor(this,R.color.defaultColor);
    }

    // return value for green color for possible moves and builds
    private int getGreenColor() {
        return ContextCompat.getColor(this,R.color.possibleMovesColor);
    }

    // return value for blue color for blue player
    int getBlueColor(){
        return ContextCompat.getColor(this,R.color.blue);
    }

    // return value for red color for red player
    int getRedColor(){
        return ContextCompat.getColor(this, R.color.red);
    }

}
