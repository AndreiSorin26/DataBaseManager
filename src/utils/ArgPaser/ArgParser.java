package utils.ArgPaser;

import java.util.HashMap;

public class ArgParser
{
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

        return args;
    }
}
