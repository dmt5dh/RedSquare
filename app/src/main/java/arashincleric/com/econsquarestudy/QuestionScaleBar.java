package arashincleric.com.econsquarestudy;


public class QuestionScaleBar extends Question {

    int maxScale;

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
