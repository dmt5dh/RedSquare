package arashincleric.com.econsquarestudy;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Question that contains radio buttons
 */
public class QuestionRadioButtons extends Question {

    int numSelections; //Number of buttons
    ArrayList<String> selections; //Text for buttons
    public QuestionRadioButtons(String question, int numSelections, String[] selections){
        super(question);
        this.numSelections = numSelections;
        this.selections = new ArrayList<String>(Arrays.asList(selections));
    }

    public int getNumSelections(){
        return this.numSelections;
    }

    public ArrayList<String> getSelections(){
        return this.selections;
    }

    public void setNumSelections(int numSelections){
        this.numSelections = numSelections;
    }

    public void setSelections(ArrayList<String> selections){
        this.selections = selections;
    }



}
