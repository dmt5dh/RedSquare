package arashincleric.com.econsquarestudy;

/**
 * Question for scale bar
 */
public class QuestionScaleBar extends Question {

    int maxScale; //Highest number scale will go to

    public QuestionScaleBar(String question, int maxScale){
        super(question);
        this.maxScale = maxScale;
    }

    public void setMaxScale(int maxScale){
        this.maxScale = maxScale;
    }

    public int getMaxScale(){
        return maxScale;
    }
}
