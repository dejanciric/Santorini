package com.example.cira.isproject;

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
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

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
    TextView[][] views = new TextView[N][N];
    Field[][] fields = new Field[N][N];
    State currentState;
    Player turn;
    int leftPlacing;
    TextView currentMovingView;
    Field currentMovingField;
    TextView gameInfo;
    String line = "";

    String numToLetter[] = {"A", "B", "C", "D", "E"};


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
                    currentMovingView.setBackgroundColor(getDefaultColor());
                    clearGreen();
                    selectedField.playerOn = turn;
                    if (selectedField.level == 3){
                        if (turn == Player.BLUE){
                            selectedView.setBackgroundColor(getBlueColor());
                            gameInfo.setText("BLUE won!");
                        }else{
                            selectedView.setBackgroundColor(getRedColor());
                            gameInfo.setText("RED won!");
                        }
                        line+= " "+numToLetter[x]+(y+1);
                        writeTurnInOutputFile(line);
                        currentState = State.GAME_OVER;
                    }else{
                        if (turn == Player.BLUE){
                            gameInfo.setText("BLUE build: ");
                            selectedView.setBackgroundColor(getBlueColor());
                        }

                        else{
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

    private void clearGreen() {
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++){
                if (fields[i][j].isGreen){
                    fields[i][j].isGreen = false;
                    views[i][j].setBackgroundColor(getDefaultColor());
                }
            }
    }


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
