package arashincleric.com.econsquarestudy;

/**
 * Class that is a base question, other classes may extend to add more features
 */
public class Question {

    String question;

    public Question(String question){
        this.question = question;
    }

    public String getQuestion(){
        return question;
    }

    public void setQuestion(String question){
        this.question = question;
    }

}
