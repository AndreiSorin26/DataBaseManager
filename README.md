# DataBaseManager

## Overview

DataBaseManager is a command line project written in Java that manages a small quiz system. It was originally created as a university assignment and demonstrates how to keep track of users, questions, quizzes and their submissions using simple text files.

The application accepts commands such as creating users or quizzes, retrieving question lists and submitting answers. All data is stored in comma separated value (CSV) files and is loaded into memory on start. After each operation the files are rewritten so the state remains persistent.

### Example usage

Below are a few simplified examples of how commands can be invoked:

```bash
# create a new user
java Tema1 --create-user --u 'john' --p 'doe'

# add a question owned by john
java Tema1 --create-question --u 'john' --p 'doe' --text '2+2?' --type 'single' --answer-1 '4' --answer-1-is-correct '1' --answer-2 '3' --answer-2-is-correct '0'

# create a quiz with the newly added question
java Tema1 --create-quizz --u 'john' --p 'doe' --name 'math' --question-1 1
```

By relying only on plain CSV files and a minimal runtime, the program uses very little memory (only a few megabytes with the provided test data) and avoids the overhead of a full database server.

## Technical details

The code is built around a generic `DataBase` class that loads CSV rows into lists of Java objects. Each specific database (users, questions, quizzes and submissions) inherits from this class and implements the conversion logic. The core of the class is shown below:


```java
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
    ...
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
```

This class exposes helper methods such as `GetIf` and `WriteBack` and keeps a simple list in memory.

The arguments passed to the program are parsed with a small utility:

```java
        public static Args Parse(String[] arguments)
        {
            Args args = new Args();
            args.command = arguments[0].substring(1);
            args.arguments = new HashMap<>();
    
            for( int i = 1; i < arguments.length; i ++ )
            {
                String argumentKey;
                if( arguments[i].contains("'") )
                    argumentKey = arguments[i].substring(1, arguments[i].indexOf('\'') - 1);
                else
                    argumentKey = arguments[i].split(" ")[0].substring(1);
    
                String argumentValue;
                if(arguments[i].contains("'"))
                    argumentValue = arguments[i].substring(arguments[i].indexOf('\'') + 1, arguments[i].length() - 1);
                else
                    argumentValue = arguments[i].split(" ")[0];
    
                args.arguments.put(argumentKey, argumentValue);
            }
```

A lightweight JSON builder is used to format responses:

```java
    package utils.JSON;
    
    import java.util.*;
    
    public class JSON
    {
        private final SortedMap<String, Object> attributes;
        private final Boolean responseJson;
        private final Boolean spaced;
        private final String[] relativeOrder;
    
        public JSON()
        {
            this(true, null, true);
        }
        public JSON(boolean responseJson)
        {
            this(responseJson, null, true);
        }
        public JSON(boolean responseJson, String[] relativeOrder, boolean spaced)
        public JSON(boolean responseJson, String[] relativeOrder, boolean spaced)
        {
            attributes = new TreeMap<>();
            this.responseJson = responseJson;
            this.relativeOrder = relativeOrder;
            this.spaced = spaced;
        }
    
        public void put(String key, Object value)
        {
            attributes.put(key, value);
        }
    
        @Override
        public String toString()
        {
            if( responseJson )
                return "{" + "'status'" + ":'" + attributes.get("status") + "'" + "," + "'message'" + ":'" + attributes.get("message") + "'" + "}";
    
            StringBuilder jsonText = new StringBuilder("{");
            if( relativeOrder == null )
                for( String key : attributes.keySet() )
                for( String key : attributes.keySet() )
                    jsonText.append("\"").append(key).append("\"").append(spaced ? " : \"" : ":\"").append(attributes.get(key).toString()).append("\"").append(", ");
            else
            {
                for( String key : relativeOrder )
                    jsonText.append("\"").append(key).append("\"").append(spaced ? " : \"" : ":\"").append(attributes.get(key).toString()).append("\"").append(", ");
                for( String key : attributes.keySet() )
                    if(!Arrays.asList(relativeOrder).contains(key))
                        jsonText.append("\"").append(key).append("\"").append(spaced ? " : \"" : ":\"").append(attributes.get(key).toString()).append("\"").append(", ");
            }
    
    
            jsonText.deleteCharAt(jsonText.length() - 1);
            jsonText.deleteCharAt(jsonText.length() - 1);
            jsonText.append("}");
            return jsonText.toString();
        }
        public static JSON Error(String message)
        {
            JSON json = new JSON();
            json.put("status", "error");
            json.put("message", message);
            return json;
        }
    
        public static JSON Ok(String message)
        {
            JSON json = new JSON();
            json.put("status", "ok");
            json.put("message", message);
            return json;
        }
```

Answers receive a unique identifier when instantiated:

```java
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
```

This repository served as a practical exercise in object oriented design and basic persistence without relying on external databases.
