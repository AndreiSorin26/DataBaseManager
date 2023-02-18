package database;

import models.User;

public class UserDB extends DataBase<User>
{
    private Integer generalID;
    @Override
    protected User ConvertToModel(String line)
    {
        String[] values = line.split(",");
        User newUser = new User();
        newUser.username = values[1];
        newUser.password = values[2];

        return newUser;
    }

    @Override
    protected String ConvertToString(User obj)
    {
        return String.join(",", obj.id.toString(), obj.username, obj.password);
    }

    @Override
    protected String GetDbHeader() {
        return "id,username,password";
    }

    @Override
    protected Integer GetId() {
        return generalID;
    }

    @Override
    protected void SetId(Integer id) {
        generalID = id;
    }
}
