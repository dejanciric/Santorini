package com.example.cira.isproject.etf.santorini.cd150570d;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import com.example.cira.isproject.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class HumanVsHumanActivity extends AppCompatActivity {
    private static final int MY_REQUEST_PERMISSION_WRITE_EXTERNAL = 3;
    private static final int N = 5;

    private enum State{
        PLACING, SELECTING_MOVE, MOVING, BUILDING, GAME_OVER
    }

    private enum Player{
        RED, BLUE, NONE
    }

    // struct that represent one field on board and information about it's state
    private class Field{
        private Player playerOn;
        private int level;
        private boolean isGreen;
        public Field(){
            playerOn = Player.NONE;
            level = 0;
            isGreen = false;
        }
    }

    private TextView[][] views = new TextView[N][N];
    private Field[][] fields = new Field[N][N];
    private State currentState;
    private Player turn;
    private int leftPlacing;
    private TextView currentMovingView;
    private Field currentMovingField;
    private TextView gameInfo;
    private String line = "";
    private String numToLetter[] = {"A", "B", "C", "D", "E"};

    // initial method that execute on activity start
    // initialize some structures and check if read from file is checked
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_human_vs_human);
        turn = Player.BLUE;
        leftPlacing = 2;
        currentState = State.PLACING;
        gameInfo = findViewById(R.id.gameInfo);
        gameInfo.setText("BLUE placing, count: "+leftPlacing);
        GridLayout gridLayout = findViewById(R.id.gridLayout);

        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                fields[i][j] = new Field();
                views[i][j] = (TextView)gridLayout.getChildAt(i*N+j);
            }
        clearOutputFile();

        Intent intent = getIntent();
        boolean readFile = intent.getBooleanExtra("readFile",false);

        if (readFile)
            readFromInputFile();

    }

    // Main logic of game, called when field is pressed
    // execute code based on the current state of the game (PLACING, SELECTING_MOVE, MOVING, BUILDING, GAME_OVER) and player
    public void onFieldClick(View view) {

        String tag = (String) view.getTag();
        int x = getX(tag);
        int y = getY(tag);

        TextView selectedView = views[x][y];
        Field selectedField = fields[x][y];


        switch (currentState){
            case PLACING:
                if (selectedField.playerOn == Player.NONE){
                    if (turn == Player.BLUE){
                        leftPlacing--;
                        line += numToLetter[x]+(y+1);

                        AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
                        animation1.setDuration(1000);

                        selectedView.startAnimation(animation1);
                        selectedView.setBackgroundColor(getBlueColor());
                        selectedField.playerOn = Player.BLUE;
                        if (leftPlacing == 0){
                            leftPlacing = 2;
                            turn = Player.RED;
                            gameInfo.setText("RED placing, count: "+leftPlacing);
                            writeTurnInOutputFile(line);
                            line="";
                        }else{
                            gameInfo.setText("BLUE placing, count: "+leftPlacing);
                            line+=" ";
                        }
                    }else{
                        leftPlacing--;
                        line += numToLetter[x]+(y+1);
                        AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
                        animation1.setDuration(1000);

                        selectedView.startAnimation(animation1);
                        selectedView.setBackgroundColor(getRedColor());
                        selectedField.playerOn = Player.RED;
                        if (leftPlacing == 0){
                            turn = Player.BLUE;
                            currentState = State.SELECTING_MOVE;
                            gameInfo.setText("BLUE select: ");
                            writeTurnInOutputFile(line);
                            line="";
                        }else{
                            gameInfo.setText("RED placing, count: "+leftPlacing);
                            line+=" ";
                        }
                    }

                }

                break;
            case SELECTING_MOVE:
                if (turn == Player.BLUE && selectedField.playerOn == Player.BLUE){
                    colorPossibleMoves(x, y);
                    if (!chechIfCanMove()){
                        currentState = State.SELECTING_MOVE;
                    }else{
                        gameInfo.setText("BLUE move: ");
                        currentMovingField = selectedField;
                        currentMovingView = selectedView;
                        currentState = State.MOVING;
                        line+=numToLetter[x]+(y+1);
                    }

                }else if(turn == Player.RED && selectedField.playerOn == Player.RED){
                    colorPossibleMoves(x, y);
                    if (!chechIfCanMove()){
                        currentState = State.SELECTING_MOVE;
                    }else{
                        gameInfo.setText("RED move: ");
                        currentMovingField = selectedField;
                        currentMovingView = selectedView;
                        currentState = State.MOVING;
                        line+=numToLetter[x]+(y+1);
                    }

                }

                break;
            case MOVING:
                if (selectedField.isGreen){
                    currentMovingField.playerOn = Player.NONE;
                    AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
                    animation1.setDuration(1000);

                    selectedView.startAnimation(animation1);
                    currentMovingView.setBackgroundColor(getDefaultColor());
                    clearGreen();
                    selectedField.playerOn = turn;
                    if (selectedField.level == 3){
                        if (turn == Player.BLUE){
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
                        currentState = State.GAME_OVER;
                    }else{
                        if (turn == Player.BLUE){
                             animation1 = new AlphaAnimation(0.2f, 1.0f);
                            animation1.setDuration(1000);

                            selectedView.startAnimation(animation1);
                            gameInfo.setText("BLUE build: ");
                            selectedView.setBackgroundColor(getBlueColor());
                        }

                        else{
                             animation1 = new AlphaAnimation(0.2f, 1.0f);
                            animation1.setDuration(1000);

                            selectedView.startAnimation(animation1);
                            gameInfo.setText("RED build: ");
                            selectedView.setBackgroundColor(getRedColor());
                        }
                        line+= " "+numToLetter[x]+(y+1);

                        colorPossibleBuilds(x,y);
                        currentState = State.BUILDING;
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
                    if (turn == Player.BLUE){
                        gameInfo.setText("RED select: ");
                        turn = Player.RED;
                    }

                    else{
                        gameInfo.setText("BLUE select: ");
                        turn = Player.BLUE;
                    }
                    line+= " "+numToLetter[x]+(y+1);
                    writeTurnInOutputFile(line);
                    line = "";
                    currentState = State.SELECTING_MOVE;
                    checkIsGameOver();
                }
                break;
        }


    }

    // check if the game is over (someone won)
    private void checkIsGameOver() {
        if (turn == Player.BLUE){
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++){
                    if (fields[i][j].playerOn == Player.BLUE){
                        colorPossibleMoves(i,j);
                    }
                }

            if (!chechIfCanMove()){
                gameInfo.setText("RED won! BLUE can't move.");

                currentState = State.GAME_OVER;
            }
            clearGreen();

        }else{
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++){
                    if (fields[i][j].playerOn == Player.RED){
                        colorPossibleMoves(i,j);
                    }
                }

            if (!chechIfCanMove()){
                gameInfo.setText("BLUE won! RED can't move.");

                currentState = State.GAME_OVER;
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
        if (x-1 >= 0 && fields[x-1][y].playerOn == Player.NONE && fields[x-1][y].level < 4){
            fields[x-1][y].isGreen = true;
            views[x-1][y].setBackgroundColor(getGreenColor());
        }
        if (x+1 < N && fields[x+1][y].playerOn == Player.NONE && fields[x+1][y].level < 4){
            fields[x+1][y].isGreen = true;
            views[x+1][y].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && fields[x][y-1].playerOn == Player.NONE && fields[x][y-1].level < 4){
            fields[x][y-1].isGreen = true;
            views[x][y-1].setBackgroundColor(getGreenColor());
        }
        if (y+1 < N && fields[x][y+1].playerOn == Player.NONE && fields[x][y+1].level < 4){
            fields[x][y+1].isGreen = true;
            views[x][y+1].setBackgroundColor(getGreenColor());
        }

        if (y+1 < N && x+1 < N && fields[x+1][y+1].playerOn == Player.NONE && fields[x+1][y+1].level < 4){
            fields[x+1][y+1].isGreen = true;
            views[x+1][y+1].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && x-1 >= 0 && fields[x-1][y-1].playerOn == Player.NONE && fields[x-1][y-1].level < 4){
            fields[x-1][y-1].isGreen = true;
            views[x-1][y-1].setBackgroundColor(getGreenColor());
        }
        if (y+1 < N && x-1 >= 0 && fields[x-1][y+1].playerOn == Player.NONE && fields[x-1][y+1].level < 4){
            fields[x-1][y+1].isGreen = true;
            views[x-1][y+1].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && x+1 < N && fields[x+1][y-1].playerOn == Player.NONE && fields[x+1][y-1].level < 4){
            fields[x+1][y-1].isGreen = true;
            views[x+1][y-1].setBackgroundColor(getGreenColor());
        }
    }

    // default color all fields that was green
    private void clearGreen() {
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                if (fields[i][j].isGreen){
                    fields[i][j].isGreen = false;
                    views[i][j].setBackgroundColor(getDefaultColor());
                }
            }
    }

    // green color all possible moves for selected figure but only in stucture not in gui
    private void colorPossibleMoves(int x, int y) {

        if (x-1 >= 0 && fields[x-1][y].playerOn == Player.NONE && difference(x,y,x-1,y) <= 1 && fields[x-1][y].level < 4){
            fields[x-1][y].isGreen = true;
            views[x-1][y].setBackgroundColor(getGreenColor());
        }
        if (x+1 < N && fields[x+1][y].playerOn == Player.NONE && difference(x,y,x+1,y) <= 1 && fields[x+1][y].level < 4){
            fields[x+1][y].isGreen = true;
            views[x+1][y].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && fields[x][y-1].playerOn == Player.NONE && difference(x,y,x,y-1) <= 1 && fields[x][y-1].level < 4){
            fields[x][y-1].isGreen = true;
            views[x][y-1].setBackgroundColor(getGreenColor());
        }
        if (y+1 < N && fields[x][y+1].playerOn == Player.NONE && difference(x,y,x,y+1) <= 1 && fields[x][y+1].level < 4){
            fields[x][y+1].isGreen = true;
            views[x][y+1].setBackgroundColor(getGreenColor());
        }

        if (y+1 < N && x+1 < N && fields[x+1][y+1].playerOn == Player.NONE && difference(x,y,x+1,y+1) <= 1 && fields[x+1][y+1].level < 4){
            fields[x+1][y+1].isGreen = true;
            views[x+1][y+1].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && x-1 >= 0 && fields[x-1][y-1].playerOn == Player.NONE && difference(x,y,x-1,y-1) <= 1 && fields[x-1][y-1].level < 4){
            fields[x-1][y-1].isGreen = true;
            views[x-1][y-1].setBackgroundColor(getGreenColor());
        }
        if (y+1 < N && x-1 >= 0 && fields[x-1][y+1].playerOn == Player.NONE && difference(x,y,x-1,y+1) <= 1 && fields[x-1][y+1].level < 4){
            fields[x-1][y+1].isGreen = true;
            views[x-1][y+1].setBackgroundColor(getGreenColor());
        }
        if (y-1 >= 0 && x+1 < N && fields[x+1][y-1].playerOn == Player.NONE && difference(x,y,x+1,y-1) <= 1 && fields[x+1][y-1].level < 4){
            fields[x+1][y-1].isGreen = true;
            views[x+1][y-1].setBackgroundColor(getGreenColor());
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

    // write just performed turn in output.txt
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
