# Light flame

Ligth flame is a modern Era ultra **light height web framework** based on netty and made for people who  like to have more control over the application, since the input of the data entrance, to the output. Everything is highly configurable... 

If you want to configure all by yourself, and have all control of your code, business logical without infer directly with the framework, so, light flame its perfectly made for you. We also encourage you to use it In a way that your code could be totally readable and decouple from the infra parts.


- low level, low abstraction , high performance, fast compile and start time
- Composite everywhere and everything, **its functions babe!**
- microservices mindset, without application services (no tomcat, jboss....)
- high configurability, before and during running time
- testing everything, intregrate tests as pure functions
- no mixing between domain and infra
- reactive non blocking IO
- non reflections, annotations free (thanks god!!)
- do whatever you want, we are on the ground =P

 
You can have some of our boiler plates example of projects using our engine and some principles of DDD, and microservices structure. I hope you enjoy, use it and help us to improve even more this simple code.

# Instalation

Using maven, declare dependency:
```maven
<dependency>
	<groupId>com.github.light-flame</groupId>
	<artifactId>lightflame-core</artifactId>
	<version>0.9.2</version>
</dependency>
```

# Quick Start


```java
package init;

import io.lightflame.bootstrap.LightFlame;

public class App {
    public static void main( String[] args ) {
        new LightFlame()
                .addBasicLog4jConfig()
                .addConfiguration(new HandlerConfig().setDefautHandlers(), null)
                .addHttpAndWsListener(8080)
                .start(App.class);
    }
}

```

create a class that contain the configuration function, in this example, HandleConfig:
```java
package init;

import io.lightflame.bootstrap.ConfigFunction;
import io.lightflame.http.FlameHttpStore;

public class HandlerConfig {

    public ConfigFunction setDefautHandlers() {
        return (config) -> {
            Handler handler = new Handler();

            // flame store
            FlameHttpStore fs =  new FlameHttpStore("/api");

            fs.R().httpGET("/hello/world/simple", handler.simpleGreeting());

            return null;
        };
    }
}
```
Now you can declare the simple handler:
```java
package init;

import io.lightflame.bootstrap.Flame;
import io.lightflame.http.FlameHttpContext;
import io.lightflame.http.FlameHttpResponse;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class Handler {

    Flame<FlameHttpContext, FlameHttpResponse> simpleGreeting() {
        return (ctx) -> {
            String name = ctx.getRequest().content().toString(CharsetUtil.UTF_8);
            String greeting = String.format("hello %s", name);
            return new FlameHttpResponse(HttpResponseStatus.OK, Unpooled.copiedBuffer(greeting, CharsetUtil.UTF_8));
        };
    }
}
```

# Routing rules

There is a bunch of existing rules that you can to route to your handler. The **FlameHttpStore** provides a way to store your functions and generate the rule for all.

```java
package routing;

import io.lightflame.bootstrap.ConfigFunction;
import io.lightflame.http.FlameHttpStore;

public class HandlerConfig {

    public ConfigFunction setDefautHandlers() {
        return (config) -> {
            Handler handler = new Handler();


            // flame store
            FlameHttpStore  fs  =  new  FlameHttpStore("/api");

            fs.R().httpGET("/*", handler.simpleGreeting()); // widecard route
            fs.R().httpGET("/path/to/my/url", handler.simpleGreeting());
            fs.R().httpGET("/hello/{name}", handler.simpleGreeting()); // dynamic route
            fs.R().httpPOST("/this/is/a/post", handler.simpleGreeting());
            // complex filters
            fs.R()
                .headerRule("x-auth","abc")
                .queryRule("name","daniel")
                .pathRule("name","daniel")
                .httpALL("/*", handler.simpleGreeting());

            return null;
        };
    }
}
```

# Router middleware
TODO: on next release

# Testing

Lightflame provides a simple way to test you application. You can test either if the route works depending on request, and all the steps throw the route.  

```java
package init;

public class TestingHandler {
}

```
# Multi port

You can open multiple ports to work. and declare different or the same function to each port. Look at this simple example how it works. Its simple as look like:

```java
package multihttp;

import io.lightflame.bootstrap.LightFlame;

public class App 
{
    public static void main( String[] args )
    {
        new LightFlame()
                .addBasicLog4jConfig()
                .addHttpAndWsListener(8080)
                .addHttpAndWsListener(8090)
                .start(App.class);
    }
}

```

then when you configure the project you just have to declare the port on constructor of **FlameHttpStore** like this:

```java
package multihttp;

import io.lightflame.bootstrap.ConfigFunction;
import io.lightflame.http.FlameHttpStore;
import routing.Handler;

public class HandlerConfig {

    public ConfigFunction setDefautHandlers() {
        return (config) -> {
            Handler handler = new Handler();

            // flame store to port 8080
            FlameHttpStore fs1 = new FlameHttpStore(8080,"/api");

            fs1.R().httpGET("/*", handler.simpleGreeting()); // widecard route
            fs1.R().httpGET("/path/to/my/url", handler.simpleGreeting());

            // flame store to port 8080
            FlameHttpStore fs2 = new FlameHttpStore(8090,"/api");

            fs2.R().httpGET("/*", handler.simpleGreeting()); // widecard route
            fs2.R().httpGET("/path/to/my/url", handler.simpleGreeting());

            return null;
        };
    }
}
```

# Web Socket and Static files (2 in 1)

You can access the full example on **flame-examples** repository on github. In this one we openned two different ports, one to serve the static files and another to WS, but you can do it using the same port

```java
package com.ws;

import io.lightflame.bootstrap.LightFlame;

public class App {
    public static void main(String[] args) {
        new LightFlame()
                .addConfiguration(new Config().setDefautHandlers(), null)
                .addBasicLog4jConfig()
                .addHttpAndWsListener(8080) // 1
                .addHttpAndWsListener(8081) // 2
                .start(App.class);
        }
}

```
In this example we:

1 - add a listener on port 8080 to static file
2 - add a listener on port 8081 to websocket

```java
package com.ws;

import io.lightflame.bootstrap.ConfigFunction;
import io.lightflame.http.FlameHttpStore;
import io.lightflame.websocket.FlameWsStore;

public class Config {

    public ConfigFunction setDefautHandlers() {
        return (config) -> {

            // http
            StaticHandler sHandler = new StaticHandler();
            FlameHttpStore httpStore = new FlameHttpStore(8080); // 3
            httpStore.R().httpGET("/", sHandler.getRootFile().and(sHandler.proccess())); // 4
            httpStore.R().httpGET("/static/*", sHandler.getOtherFiles().and(sHandler.proccess())); // 5

            // websocket
            WsHandler wsHandler = new WsHandler();
            FlameWsStore wsStore =  new FlameWsStore(8081); // 3
            wsStore.R().path("/ws", wsHandler.webSocketFunc()); // 6

            return null;
        };
    }
    
}
```

3 - Its important to use the same port declared in the main file to instanciate the stores. If you dont do it the handler will throw an error.

4 - the first handler execute two functions on context "/" (getRootFile() -> proccess()). This one is responsible to get the root index.html file and the second one process the file

5 - this handler execute function on a widecard context after "/static/*". This happens to bring all other files that html will call, like style and js. In this one we used another function as the first one, but we mantain the second one, since the process is the same.

```java
package com.ws;

import io.lightflame.bootstrap.Flame;
import io.lightflame.http.FlameHttpContext;
import io.lightflame.http.FlameHttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.io.FileInputStream;

public class StaticHandler {

    Flame<FlameHttpContext, File> getRootFile() { // 4
        return (ctx) -> {
            return new File(getClass().getClassLoader().getResource("dist/index.html").getFile());
        };
    }

    Flame<FlameHttpContext, File> getOtherFiles() { // 5
        return (ctx) -> {
            String url = ctx.getPathWithoutPrefix();
            return new File(getClass().getClassLoader().getResource("dist/" + url).getFile());
        };
    }

    Flame<File, FlameHttpResponse> proccess() { // 4 and 5
        return (ctx) -> {
            FileInputStream inFile = new FileInputStream(ctx);
            ByteBuf buffer = Unpooled.copiedBuffer(inFile.readAllBytes());
            inFile.close();
            return new FlameHttpResponse(HttpResponseStatus.OK, buffer);
        };
    }
}
```

Now we have the static hander with our three functions. The last one is the main function that receive as parameter a File and return the final **FlameHttpResponse**. The first two functions are called from different routes to read the file depending on context.

```java
package com.ws;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import io.lightflame.bootstrap.Flame;
import io.lightflame.websocket.FlameWsContext;
import io.lightflame.websocket.FlameWsResponse;


public class WsHandler {

    private int i = -1;

    private List<String> messages = Arrays.asList( "Hi there, I\"m Fabio and you?", "Nice to meet you", "How are you?", "Not too bad, thanks", "What do you do?", "That\"s awesome", "Codepen is a nice place to stay", "I think you\'re a nice person", "Why do you think that?", "Can you explain?", "Anyway I\'ve gotta go now", "It was a pleasure chat with you", "Time to make a new codepen", "Bye", ":)");

    Flame<FlameWsContext, FlameWsResponse> webSocketFunc() { // 6
        return (ctx) -> {
            i++;
            return new FlameWsResponse(this.messages.get(i));
        };
    }
}
```

at the end we have our final handler for websocket app.