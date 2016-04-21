Unlike JBoss and Glassfish there is no way to define a custom JNDI object (eg a URL) in the WebLogic console. This provides a very simple way to do that.

For example we use this to define the location of various configuration files (see maduraconfiguration in google code) and we locate these files using a URL defined in JNDI.

To use this you just need to put the jar file into your `<domain>/server/lib directory` (I tested this in `<BEA>/wlserver_10.0/samples/domains/wl_server/lib` using WebLogic 10.0)

Then go into the console and create a startup/shutdown class (the page is called Startup and Shutdown Classes and you add one). Your page should look like this:

<img src='http://weblogic-jndi-startup.googlecode.com/files/jndi.png' height='400px' />

You enter the class name (always nz.co.senanque.jndi.WebLogicJndiStartup).
I set 'Failure is Fatal' but be careful, if you do something wrong your server won't start.
You do want it to run before you applications start (the last check box).

But most important is the Arguments. The one shown is:

`configURL<java.net.URL>=file:/abc/def`

The JNDI name is, therefore, configURL and the type is a URL. The value is `file:/abc/def`
If you specify a type the type must
  * be on the classpath
  * have a constructor that takes a single String argument.
The value string (`file:/abc/def`) is passed to the constructor and the result placed in JNDI.

To access it from your application you can use something like this:

`Context context = new InitialContext();`

`URL config = (URL)context.lookup("configURL");`

`System.out.println("configURL="+String.valueOf(config));`

Note that you can have as many of these startup classes as you want. You can reuse this same class over and over with different arguments. We typically have just one but your needs might be different.