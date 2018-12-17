package com.example.cira.isproject.etf.santorini.cd150570d;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.cira.isproject.R;

public class MainActivity extends AppCompatActivity {

    private enum TypeOfGame{
        HumanVsHuman, HumanVsComputer, ComputerVsComputer
    }

    private TypeOfGame typeofGame;
    private String difficulty;
    private Spinner spinner;
    private TextView tv;

    // initial method that execute on program start
    // initialize some structures and listeners
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = findViewById(R.id.spinner);
        tv = findViewById(R.id.tv);

        ((RadioButton)findViewById(R.id.rb1)).setChecked(true);
        typeofGame = TypeOfGame.HumanVsHuman;
        spinner.setAlpha(0f);
        tv.setAlpha(0f);
        spinner.setEnabled(false);
        spinner.setVisibility(View.INVISIBLE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.difficulty, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                difficulty = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    // called when start game button pressed
    // based on selected type of game start corresponding activity and pass them some information
    public void StartGame(View view) {
        boolean readFile = false;
        CheckBox cb = findViewById(R.id.cb);
        if (cb.isChecked()){
            readFile = true;
        }
        Intent intent;
        switch(typeofGame){
            case HumanVsHuman:
                intent = new Intent(this, HumanVsHumanActivity.class);
                intent.putExtra("readFile", readFile);
                startActivity(intent);
                break;
            case HumanVsComputer:
                intent = new Intent(this, HumanVsComputerActivity.class);
                intent.putExtra("readFile", readFile);
                intent.putExtra("difficulty", difficulty);
                startActivity(intent);
                break;
            case ComputerVsComputer:
                intent = new Intent(this, ComputerVsComputerActivity.class);
                intent.putExtra("readFile", readFile);
                intent.putExtra("difficulty", difficulty);
                startActivity(intent);
                break;
        }
    }

    // called when radio button for type of game is changed
    // to remember the choice and possibly show spinner with difficulties for computer
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()){
            case R.id.rb1:
                if (checked){
                    typeofGame = TypeOfGame.HumanVsHuman;
                    spinner.setAlpha(0f);
                    tv.setAlpha(0f);
                    spinner.setEnabled(false);
                    spinner.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.rb2:
                if (checked){
                    typeofGame = TypeOfGame.HumanVsComputer;
                    spinner.setAlpha(1f);
                    tv.setAlpha(1f);
                    spinner.setEnabled(true);
                    spinner.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.rb3:
                if (checked){
                    typeofGame = TypeOfGame.ComputerVsComputer;
                    spinner.setAlpha(1f);
                    tv.setAlpha(1f);
                    spinner.setEnabled(true);
                    spinner.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

}
