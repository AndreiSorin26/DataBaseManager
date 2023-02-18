package models;

import java.util.List;

public class Submission implements IModel
{
    public Integer id;
    public Integer submitterId;
    public Integer quizId;
    public List<Integer> answers;
    public Integer score;

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }
}
