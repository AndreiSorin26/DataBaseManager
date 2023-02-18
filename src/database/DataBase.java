package database;

import models.IModel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class DataBase<Model extends IModel>
{
    protected abstract Model ConvertToModel(String line);
    protected abstract String ConvertToString(Model obj);
    protected abstract String GetDbHeader();
    protected abstract Integer GetId();
    protected abstract void SetId(Integer id);

    private List<Model> db = null;

    public void Load(String fileName)
    {
        SetId(1);
        try (BufferedReader br = new BufferedReader(new FileReader(fileName)))
        {
            this.db = new ArrayList<>();
            String line = br.readLine(); ///ignore the header
            while ((line = br.readLine()) != null)
            {
                Model instance = ConvertToModel(line);
                instance.setId(GetId());
                this.db.add(instance);
                SetId(GetId() + 1);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Model GetById(Integer id)
    {
        for( Model obj : this.db )
            if( obj.getId().equals(id) )
                return obj;
        return null;
    }

    public List<Model> GetIf(Function<Model, Boolean> condition)
    {
        List<Model> result = new ArrayList<>();
        for( Model obj : this.db )
            if( condition.apply(obj) )
                result.add(obj);
        return result;
    }

    public Model GetOneIf(Function<Model, Boolean> condition)
    {
        for( Model obj : this.db )
            if( condition.apply(obj) )
                return obj;
        return null;
    }

    public Integer Count(Function<Model, Boolean> condition)
    {
        Integer count = 0;
        for( Model obj : this.db )
            if( condition.apply(obj) )
                count++;
        return count;
    }

    public boolean Exists(Function<Model, Boolean> condition)
    {
        return Count(condition) > 0;
    }

    public void Put(Model obj)
    {
        obj.setId(GetId());
        SetId(GetId() + 1);
        this.db.add(obj);
    }

    public void WriteBack(File file)
    {
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw))
        {

            out.println(GetDbHeader());
            for(Model obj : this.db)
                out.println(ConvertToString(obj));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void DeleteIf(Function<Model, Boolean> condition)
    {
        List<Model> toBeEliminated = this.db.stream().filter(condition::apply).collect(Collectors.toList());
        for( Model obj : toBeEliminated )
            this.db.remove(obj);
        ResetIds();
    }

    public void Delete(Model obj)
    {
        DeleteIf(o -> o == obj);
    }

    public void Clean(String fileName)
    {
        try (FileWriter fw = new FileWriter(fileName);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw))
        {
            out.println(GetDbHeader());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ResetIds()
    {
        for(int i = 0; i < this.db.size(); i++)
            this.db.get(i).setId(i + 1);
        SetId(this.db.size());
    }
}
