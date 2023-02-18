package models;

import java.util.List;

public class Quiz implements IModel
{
    public Integer id;
    public Integer ownerId;
    public String name;
    public List<Integer> questionIds;

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }
}
