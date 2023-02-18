package models;

public class User implements IModel
{
    public Integer id;
    public String username;
    public String password;

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }
}
