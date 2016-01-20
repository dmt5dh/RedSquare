package arashincleric.com.econsquarestudy;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Dan on 1/18/2016.
 */
public class QuestionRadioButtons extends Question {

    int numSelections;
    ArrayList<String> selections;
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
