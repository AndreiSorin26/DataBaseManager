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
}
