package com.example.cira.isproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.transform.Templates;

public class HumanVsComputerActivity extends AppCompatActivity {
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

    private class AIMove{
        private int xFigure, yFigure, xMove, yMove, xBuild, yBuild;
        private int score;
        private int alpha, beta;

        public AIMove(){

        }
        public AIMove(int sc){ score = sc;}


    }

    private class Field{
        private HumanVsComputerActivity.Player playerOn;
        private int level;
        private boolean isGreen;
        public Field(){
            playerOn = HumanVsComputerActivity.Player.NONE;
            level = 0;
            isGreen = false;
        }
    }
    TextView[][] views = new TextView[N][N];
    HumanVsComputerActivity.Field[][] fields = new HumanVsComputerActivity.Field[N][N];
    HumanVsComputerActivity.State currentState;
    HumanVsComputerActivity.Player turn;
    int leftPlacing;
    TextView currentMovingView;
    HumanVsComputerActivity.Field currentMovingField;
    TextView gameInfo;
    String line = "";
    String difficulty;
    boolean readFile;

    String numToLetter[] = {"A", "B", "C", "D", "E"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_human_vs_computer);
        turn = HumanVsComputerActivity.Player.BLUE;
        leftPlacing = 2;
        currentState = HumanVsComputerActivity.State.PLACING;
        gameInfo = findViewById(R.id.gameInfo);
        gameInfo.setText("BLUE placing, count: "+leftPlacing);
        GridLayout gridLayout = findViewById(R.id.gridLayout);

        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                fields[i][j] = new HumanVsComputerActivity.Field();
                views[i][j] = (TextView)gridLayout.getChildAt(i*N+j);
            }
        clearOutputFile();

        Intent intent = getIntent();
        readFile = intent.getBooleanExtra("readFile",false);
        difficulty = intent.getStringExtra("difficulty");
        if (readFile)
            readFromInputFile();

    }

    public void onFieldClick(View view) {

        String tag = (String) view.getTag();
        int x = getX(tag);
        int y = getY(tag);

        TextView selectedView = views[x][y];
        HumanVsComputerActivity.Field selectedField = fields[x][y];


        switch (currentState){
            case PLACING:
                if (selectedField.playerOn == HumanVsComputerActivity.Player.NONE){
                    if (turn == HumanVsComputerActivity.Player.BLUE){
                        leftPlacing--;
                        line += numToLetter[x]+(y+1);
                        selectedView.setBackgroundColor(getBlueColor());
                        selectedField.playerOn = HumanVsComputerActivity.Player.BLUE;
                        if (leftPlacing == 0){
                            leftPlacing = 2;
                            turn = HumanVsComputerActivity.Player.RED;
                            gameInfo.setText("RED placing, count: "+leftPlacing);
                            writeTurnInOutputFile(line);
                            line="";

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


                        }else{
                            gameInfo.setText("BLUE placing, count: "+leftPlacing);
                            line+=" ";
                        }
                    }else{
                        leftPlacing--;
                        line += numToLetter[x]+(y+1);
                        selectedView.setBackgroundColor(getRedColor());
                        selectedField.playerOn = HumanVsComputerActivity.Player.RED;
                        if (leftPlacing == 0){
                            turn = HumanVsComputerActivity.Player.BLUE;
                            currentState = HumanVsComputerActivity.State.SELECTING_MOVE;
                            gameInfo.setText("BLUE select: ");
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
                if (turn == HumanVsComputerActivity.Player.BLUE && selectedField.playerOn == HumanVsComputerActivity.Player.BLUE){
                    colorPossibleMoves(x, y);
                    if (!chechIfCanMove()){
                        currentState = HumanVsComputerActivity.State.SELECTING_MOVE;
                    }else{
                        gameInfo.setText("BLUE move: ");
                        currentMovingField = selectedField;
                        currentMovingView = selectedView;
                        currentState = HumanVsComputerActivity.State.MOVING;
                        line+=numToLetter[x]+(y+1);
                    }

                }else if(turn == HumanVsComputerActivity.Player.RED && selectedField.playerOn == HumanVsComputerActivity.Player.RED){
                    colorPossibleMoves(x, y);
                    if (!chechIfCanMove()){
                        currentState = HumanVsComputerActivity.State.SELECTING_MOVE;
                    }else{
                        gameInfo.setText("RED move: ");
                        currentMovingField = selectedField;
                        currentMovingView = selectedView;
                        currentState = HumanVsComputerActivity.State.MOVING;
                        line+=numToLetter[x]+(y+1);
                    }

                }

                break;
            case MOVING:
                if (selectedField.isGreen){
                    currentMovingField.playerOn = HumanVsComputerActivity.Player.NONE;
                    currentMovingView.setBackgroundColor(getDefaultColor());
                    clearGreen();
                    selectedField.playerOn = turn;
                    if (selectedField.level == 3){
                        if (turn == HumanVsComputerActivity.Player.BLUE){
                            selectedView.setBackgroundColor(getBlueColor());
                            gameInfo.setText("BLUE won!");
                        }else{
                            selectedView.setBackgroundColor(getRedColor());
                            gameInfo.setText("RED won!");
                        }
                        line+= " "+numToLetter[x]+(y+1);
                        writeTurnInOutputFile(line);
                        currentState = HumanVsComputerActivity.State.GAME_OVER;
                    }else{
                        if (turn == HumanVsComputerActivity.Player.BLUE){
                            gameInfo.setText("BLUE build: ");
                            selectedView.setBackgroundColor(getBlueColor());
                        }

                        else{
                            gameInfo.setText("RED build: ");
                            selectedView.setBackgroundColor(getRedColor());
                        }
                        line+= " "+numToLetter[x]+(y+1);

                        colorPossibleBuilds(x,y);
                        currentState = HumanVsComputerActivity.State.BUILDING;
                    }

                }

                break;

            case BUILDING:
                if (selectedField.isGreen){

                    clearGreen();
                    selectedField.level++;
                    selectedView.setText(selectedField.level+"");
                    if (turn == HumanVsComputerActivity.Player.BLUE){
                        gameInfo.setText("RED select: ");
                        turn = HumanVsComputerActivity.Player.RED;
                        line+= " "+numToLetter[x]+(y+1);
                        writeTurnInOutputFile(line);
                        line = "";
                        if (!readFile){
                            if (difficulty.equals("Easy")){
                                performMove(getBestMove(Player.RED, 0, null), Player.RED);
                            }else if (difficulty.equals("Medium")){
                                performMove(getBestMoveAlphaBeta(Player.RED, 0, null, MINUS_INF, PLUS_INF), Player.RED);
                            }else{
                                performMove(getBestMove(Player.RED, 0, null), Player.RED);
                            }

                        }


                    }

                    else{
                        gameInfo.setText("BLUE select: ");
                        turn = HumanVsComputerActivity.Player.BLUE;
                        line+= " "+numToLetter[x]+(y+1);
                        writeTurnInOutputFile(line);
                        line = "";
                    }

                    if (currentState != State.GAME_OVER)
                        currentState = HumanVsComputerActivity.State.SELECTING_MOVE;
                    checkIsGameOver();
                }

                break;
        }


    }

    private AIMove getBestMoveAlphaBeta(Player player, int h, AIMove moveForHeuristic, int alpha, int beta) {

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
                move.score = getBestMoveAlphaBeta(Player.BLUE, ++h, move, alpha, beta).score;
            }else{
                move.score = getBestMoveAlphaBeta(Player.RED, ++h, move, alpha, beta).score;
            }
            h--;
            if (h == 1){
                alpha = move.score;
            }else if (h == 0){
                beta = move.alpha;
            }
            oneNodeMoves.add(move);
            undoMove(move, player);
            if (alpha >= beta)
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
        if (moveForHeuristic != null)
            oneNodeMoves.get(bestMoveIndex).score -= heuristicFunction(oneNodeMoves.get(bestMoveIndex), Player.RED);

        return oneNodeMoves.get(bestMoveIndex);
    }

    private void performMove(AIMove bestMove, Player red) {
        currentState = State.SELECTING_MOVE;
        onFieldClick(views[bestMove.xFigure][bestMove.yFigure]);
        onFieldClick(views[bestMove.xMove][bestMove.yMove]);
        onFieldClick(views[bestMove.xBuild][bestMove.yBuild]);
        //currentState = State.BUILDING;

    }

    AIMove getBestMove(Player player, int h, AIMove moveForHeuristic){

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
            undoMove(moveForHeuristic, Player.RED);
            oneNodeMoves.get(bestMoveIndex).score -= heuristicFunction(moveForHeuristic, Player.RED);
            doMove(moveForHeuristic, Player.RED);
        }


       return oneNodeMoves.get(bestMoveIndex);

    }

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

    // calculate substraction of 2 players figures
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

    // calculate distance for player, sum of both figures distances
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

    private void doMove(AIMove move, Player player) {
        fields[move.xFigure][move.yFigure].playerOn = Player.NONE;
        fields[move.xMove][move.yMove].playerOn = player;
        fields[move.xBuild][move.yBuild].level++;
    }
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

    private void checkIsGameOver() {
        if (turn == HumanVsComputerActivity.Player.BLUE){
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++){
                    if (fields[i][j].playerOn == HumanVsComputerActivity.Player.BLUE){
                        colorPossibleMoves(i,j);
                    }
                }

            if (!chechIfCanMove()){
                gameInfo.setText("RED won! BLUE can't move.");

                currentState = HumanVsComputerActivity.State.GAME_OVER;
            }
            clearGreen();

        }else{
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++){
                    if (fields[i][j].playerOn == HumanVsComputerActivity.Player.RED){
                        colorPossibleMoves(i,j);
                    }
                }

            if (!chechIfCanMove()){
                gameInfo.setText("BLUE won! RED can't move.");

                currentState = HumanVsComputerActivity.State.GAME_OVER;
            }
            clearGreen();

        }

    }

    private boolean isGameOver(Player player){
        if (player == HumanVsComputerActivity.Player.BLUE){
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++){
                    if (fields[i][j].playerOn == HumanVsComputerActivity.Player.BLUE){
                        colorPossibleMovesLogical(i,j);
                    }
                }

            if (!chechIfCanMove()){
                return true;
            }
            clearGreenLogical();

        }else{
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++){
                    if (fields[i][j].playerOn == HumanVsComputerActivity.Player.RED){
                        colorPossibleMovesLogical(i,j);
                    }
                }

            if (!chechIfCanMove()){
                return true;
            }
            clearGreenLogical();

        }
        return false;
    }

    private boolean chechIfCanMove() {
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                if (fields[i][j].isGreen){
                    return true;
                }
            }
        return false;
    }

    private void colorPossibleBuilds(int x, int y) {
        if (x-1 >= 0 && fields[x-1][y].playerOn == HumanVsComputerActivity.Player.NONE && fields[x-1][y].level < 4){
            fields[x-1][y].isGreen = true;
            views[x-1][y].setBackgroundColor(getGreenColor());
        }
        if (x+1 < N && fields[x+1][y].playerOn == HumanVsComputerActivity.Player.NONE && fields[x+1][y].level < 4){
            fields[x+1][y].isGreen = true;
            views[x+1][y].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && fields[x][y-1].playerOn == HumanVsComputerActivity.Player.NONE && fields[x][y-1].level < 4){
            fields[x][y-1].isGreen = true;
            views[x][y-1].setBackgroundColor(getGreenColor());
        }
        if (y+1 < N && fields[x][y+1].playerOn == HumanVsComputerActivity.Player.NONE && fields[x][y+1].level < 4){
            fields[x][y+1].isGreen = true;
            views[x][y+1].setBackgroundColor(getGreenColor());
        }

        if (y+1 < N && x+1 < N && fields[x+1][y+1].playerOn == HumanVsComputerActivity.Player.NONE && fields[x+1][y+1].level < 4){
            fields[x+1][y+1].isGreen = true;
            views[x+1][y+1].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && x-1 >= 0 && fields[x-1][y-1].playerOn == HumanVsComputerActivity.Player.NONE && fields[x-1][y-1].level < 4){
            fields[x-1][y-1].isGreen = true;
            views[x-1][y-1].setBackgroundColor(getGreenColor());
        }
        if (y+1 < N && x-1 >= 0 && fields[x-1][y+1].playerOn == HumanVsComputerActivity.Player.NONE && fields[x-1][y+1].level < 4){
            fields[x-1][y+1].isGreen = true;
            views[x-1][y+1].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && x+1 < N && fields[x+1][y-1].playerOn == HumanVsComputerActivity.Player.NONE && fields[x+1][y-1].level < 4){
            fields[x+1][y-1].isGreen = true;
            views[x+1][y-1].setBackgroundColor(getGreenColor());
        }
    }
    private void colorPossibleBuildsLogical(int x, int y) {
        if (x-1 >= 0 && fields[x-1][y].playerOn == HumanVsComputerActivity.Player.NONE && fields[x-1][y].level < 4){
            fields[x-1][y].isGreen = true;
        }
        if (x+1 < N && fields[x+1][y].playerOn == HumanVsComputerActivity.Player.NONE && fields[x+1][y].level < 4){
            fields[x+1][y].isGreen = true;
        }
        if (y-1 >= 0 && fields[x][y-1].playerOn == HumanVsComputerActivity.Player.NONE && fields[x][y-1].level < 4){
            fields[x][y-1].isGreen = true;
        }
        if (y+1 < N && fields[x][y+1].playerOn == HumanVsComputerActivity.Player.NONE && fields[x][y+1].level < 4){
            fields[x][y+1].isGreen = true;
        }

        if (y+1 < N && x+1 < N && fields[x+1][y+1].playerOn == HumanVsComputerActivity.Player.NONE && fields[x+1][y+1].level < 4){
            fields[x+1][y+1].isGreen = true;
        }
        if (y-1 >= 0 && x-1 >= 0 && fields[x-1][y-1].playerOn == HumanVsComputerActivity.Player.NONE && fields[x-1][y-1].level < 4){
            fields[x-1][y-1].isGreen = true;
        }
        if (y+1 < N && x-1 >= 0 && fields[x-1][y+1].playerOn == HumanVsComputerActivity.Player.NONE && fields[x-1][y+1].level < 4){
            fields[x-1][y+1].isGreen = true;
        }
        if (y-1 >= 0 && x+1 < N && fields[x+1][y-1].playerOn == HumanVsComputerActivity.Player.NONE && fields[x+1][y-1].level < 4){
            fields[x+1][y-1].isGreen = true;
        }
    }

    private void clearGreen() {
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                if (fields[i][j].isGreen){
                    fields[i][j].isGreen = false;
                    views[i][j].setBackgroundColor(getDefaultColor());
                }
            }
    }

    private void clearGreenLogical() {
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                if (fields[i][j].isGreen){
                    fields[i][j].isGreen = false;
                }
            }
    }


    private void colorPossibleMoves(int x, int y) {

        if (x-1 >= 0 && fields[x-1][y].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x-1,y) <= 1 && fields[x-1][y].level < 4){
            fields[x-1][y].isGreen = true;
            views[x-1][y].setBackgroundColor(getGreenColor());
        }
        if (x+1 < N && fields[x+1][y].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x+1,y) <= 1 && fields[x+1][y].level < 4){
            fields[x+1][y].isGreen = true;
            views[x+1][y].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && fields[x][y-1].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x,y-1) <= 1 && fields[x][y-1].level < 4){
            fields[x][y-1].isGreen = true;
            views[x][y-1].setBackgroundColor(getGreenColor());
        }
        if (y+1 < N && fields[x][y+1].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x,y+1) <= 1 && fields[x][y+1].level < 4){
            fields[x][y+1].isGreen = true;
            views[x][y+1].setBackgroundColor(getGreenColor());
        }

        if (y+1 < N && x+1 < N && fields[x+1][y+1].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x+1,y+1) <= 1 && fields[x+1][y+1].level < 4){
            fields[x+1][y+1].isGreen = true;
            views[x+1][y+1].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && x-1 >= 0 && fields[x-1][y-1].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x-1,y-1) <= 1 && fields[x-1][y-1].level < 4){
            fields[x-1][y-1].isGreen = true;
            views[x-1][y-1].setBackgroundColor(getGreenColor());
        }
        if (y+1 < N && x-1 >= 0 && fields[x-1][y+1].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x-1,y+1) <= 1 && fields[x-1][y+1].level < 4){
            fields[x-1][y+1].isGreen = true;
            views[x-1][y+1].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && x+1 < N && fields[x+1][y-1].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x+1,y-1) <= 1 && fields[x+1][y-1].level < 4){
            fields[x+1][y-1].isGreen = true;
            views[x+1][y-1].setBackgroundColor(getGreenColor());
        }
    }

    private void colorPossibleMovesLogical(int x, int y) {

        if (x-1 >= 0 && fields[x-1][y].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x-1,y) <= 1 && fields[x-1][y].level < 4){
            fields[x-1][y].isGreen = true;

        }
        if (x+1 < N && fields[x+1][y].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x+1,y) <= 1 && fields[x+1][y].level < 4){
            fields[x+1][y].isGreen = true;

        }
        if (y-1 >= 0 && fields[x][y-1].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x,y-1) <= 1 && fields[x][y-1].level < 4){
            fields[x][y-1].isGreen = true;

        }
        if (y+1 < N && fields[x][y+1].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x,y+1) <= 1 && fields[x][y+1].level < 4){
            fields[x][y+1].isGreen = true;

        }

        if (y+1 < N && x+1 < N && fields[x+1][y+1].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x+1,y+1) <= 1 && fields[x+1][y+1].level < 4){
            fields[x+1][y+1].isGreen = true;
        }
        if (y-1 >= 0 && x-1 >= 0 && fields[x-1][y-1].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x-1,y-1) <= 1 && fields[x-1][y-1].level < 4){
            fields[x-1][y-1].isGreen = true;
        }
        if (y+1 < N && x-1 >= 0 && fields[x-1][y+1].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x-1,y+1) <= 1 && fields[x-1][y+1].level < 4){
            fields[x-1][y+1].isGreen = true;
        }
        if (y-1 >= 0 && x+1 < N && fields[x+1][y-1].playerOn == HumanVsComputerActivity.Player.NONE && difference(x,y,x+1,y-1) <= 1 && fields[x+1][y-1].level < 4){
            fields[x+1][y-1].isGreen = true;
        }
    }


    private int difference(int srcX, int srcY, int dstX, int dstY) {
        return fields[dstX][dstY].level - fields[srcX][srcY].level;
    }

    int getX(String tag){
        int x = Integer.parseInt(tag);
        x = x / N;
        return x;
    }

    int getY(String tag){
        int y = Integer.parseInt(tag);
        y = y % N;
        return y;
    }

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
            if (turn == Player.RED)
                performMove(getBestMove(Player.RED, 0, null), Player.RED);
        }catch (Exception e){

        }
    }

    private int numFromFile(String s) {
        int y = Integer.parseInt(s);
        return y-1;
    }

    int letterToNum(String letter){

        for (int i = 0; i < N; i++)
            if (numToLetter[i].equals(letter))
                return i;

        return -1;
    }


    private int getDefaultColor() {
        return ContextCompat.getColor(this,R.color.defaultColor);
    }

    private int getGreenColor() {
        return ContextCompat.getColor(this,R.color.possibleMovesColor);
    }

    int getBlueColor(){
        return ContextCompat.getColor(this,R.color.blue);
    }
    int getRedColor(){
        return ContextCompat.getColor(this, R.color.red);
    }

}
