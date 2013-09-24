/*******************************************************************************
 * Copyright (c) 2013 Prometheus Consulting
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 *******************************************************************************/

package nz.co.senanque.jndi;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.Queue;

import javax.naming.*;

/**
 * 
 * See http://download.oracle.com/docs/cd/E12840_01/wls/docs103/ConsoleHelp/taskhelp/startup_shutdown/AddStartupAndShutdownClassesToTheClasspath.html
 * Also see http://objectmix.com/weblogic/524766-startup-classes-wl6-0-a.html
 * 
 * @author Roger Parkinson
 * @author jeczmien@podgorska.ddns.info
 * @version $Revision:$
 */
public class WebLogicJndiStartup
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        InitialContext ic=null;
        try{
            ic = new InitialContext();
            for (String arg: args)
            {
                parseArg(ic,arg);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Takes apart an argument. Arguments are name<type>=value
     * type field is optional and defaults to String
     * @param ic
     * @param arg
     */
    private static void parseArg(InitialContext ic, String arg)
    {
        System.out.println("Found argument: "+arg);

        int i = arg.indexOf('=');
        if (i == -1)
        {
            throw new RuntimeException("Bad argument: "+arg);
        }
        String name = arg.substring(0,i);
        Object value = arg.substring(i+1);
        i = name.indexOf('<');
        if (i != -1)
        {
            String type = name.substring(i+1,name.length()-1);
            name = name.substring(0,i);
            try
            {
                Class clazz = Class.forName(type);
                Constructor<String> constructor = clazz.getConstructor(String.class);
                value = constructor.newInstance(value);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        bindParam( ic, name, value );
    }

    private static void bindParam( InitialContext ic, String name, Object value )
    {
        System.out.println("Creating JNDI entry: "+name+" "+String.valueOf(value));
        try
        {
            Queue<String> n = split(name);
            Context nc = ic;
            while (n.size() > 1) {
                nc = checkAndCreateContext( n, nc );
            }
            bind( value, n.remove(), nc );
        }
        catch (NamingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void bind( Object value, String name, Context nc ) throws NamingException
    {
        nc.bind(name, value);
    }

    private static Context checkAndCreateContext( Queue<String> n, Context nc ) throws NamingException
    {
        String s = n.remove();
        if (checkContext( nc, s ) )
        {
            nc = (Context)nc.lookup( s );
        } else {
            nc = nc.createSubcontext( s );
        }
        return nc;
    }

    private static Queue<String> split(String name)
    {
        Queue<String> retVal = new LinkedList<String>();
        for (String s : name.split( "/" )) 
        {
            for (String ss : s.split( "\\." )) {
                retVal.add( ss );
            }
        }
        return retVal;
    }
    
    private static boolean checkContext(Context nc, String name)
    {
        boolean retVal = true;
        try
        {
            retVal = nc.lookup( name ) != null;
        }
        catch ( NamingException e )
        {
            retVal = false;
        }
        return retVal;
    }
}