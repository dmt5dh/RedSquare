package arashincleric.com.econsquarestudy;

/**
 * Question for scale bar
 */
public class QuestionScaleBar extends Question {

    int maxScale; //Highest number scale will go to
    String minText;
    String maxText;

    public QuestionScaleBar(String question, int maxScale, String minText, String maxText){
        super(question);
        this.maxScale = maxScale;
        this.minText = minText;
        this.maxText = maxText;
    }

    public void setMaxScale(int maxScale){
        this.maxScale = maxScale;
    }

    public int getMaxScale(){
        return maxScale;
    }

    public String getMaxText(){
        return maxText;
    }

    public String getMinText(){
        return minText;
    }

}
