/*
 * Created on 2/02/2011
 *
 * Copyright 2011 Prometheus Consulting.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.co.senanque.jndi;

import java.lang.reflect.Constructor;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * 
 * See http://download.oracle.com/docs/cd/E12840_01/wls/docs103/ConsoleHelp/taskhelp/startup_shutdown/AddStartupAndShutdownClassesToTheClasspath.html
 * Also see http://objectmix.com/weblogic/524766-startup-classes-wl6-0-a.html
 * 
 * @author Roger Parkinson
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
        System.out.println("Creating JNDI entry: "+name+" "+String.valueOf(value));
        try
        {
            ic.bind(name, value);
        }
        catch (NamingException e)
        {
            throw new RuntimeException(e);
        }
    }

}
