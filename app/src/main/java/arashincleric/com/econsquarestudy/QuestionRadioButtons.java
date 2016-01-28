package arashincleric.com.econsquarestudy;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Question that contains radio buttons
 */
public class QuestionRadioButtons extends Question {

    int numSelections; //Number of buttons
    ArrayList<String> selections; //Text for buttons
    boolean isCheckBox;

    public QuestionRadioButtons(String question, int numSelections, String[] selections, boolean isCheckBox){
        super(question);
        this.numSelections = numSelections;
        this.selections = new ArrayList<String>(Arrays.asList(selections));
        this.isCheckBox = isCheckBox;
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

    public boolean isCheckBox(){
        return isCheckBox;
    }



}
