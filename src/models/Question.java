package models;

import java.util.List;


public class Question implements IModel
{
    public Integer id;
    public Integer ownerId;
    public Boolean isSingle;
    public String text;
    public List<Answer> answers;

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }
}
