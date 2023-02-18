package models;

public class Answer
{
    public static Integer generalID = 0;
    public Integer ID;
    public String text;
    public Boolean isCorrect;

    public Answer()
    {
        this.ID = generalID++;
    }
}
