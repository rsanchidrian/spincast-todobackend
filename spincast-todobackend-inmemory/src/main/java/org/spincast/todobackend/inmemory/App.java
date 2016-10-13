package org.spincast.todobackend.inmemory;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spincast.core.server.Server;
import org.spincast.plugins.routing.DefaultRouter;
import org.spincast.todobackend.inmemory.controllers.TodoController;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * The main class of the application. Everything starts with the
 * classic <code>main(...)</code> method.
 */
public class App {

    protected final Logger logger = LoggerFactory.getLogger(App.class);

    /**
     * The entry point for the application.
     */
    public static void main(String[] args) {
        createApp(args, null);
    }

    /**
     * Creates an App instance using the given
     * parameters, an overriding module, and returns the 
     * Guice injector.
     * 
     * @param overridingModule Mostly useful for the integration tests. Those
     * can override some bindings by specifying this overriding module.
     */
    public static Injector createApp(String[] args, Module overridingModule) {

        if(args == null) {
            args = new String[]{};
        }

        //==========================================
        // Should we override the base app modules
        // with an overring module?
        //==========================================
        Injector guice = null;
        if(overridingModule != null) {
            guice = Guice.createInjector(Modules.override(getAppModules(args))
                                                .with(overridingModule));
        } else {
            guice = Guice.createInjector(getAppModules(args));
        }

        App website = guice.getInstance(App.class);
        website.start();

        return guice;
    }

    /**
     * The app's Guice modules to use.
     */
    protected static List<? extends Module> getAppModules(String[] args) {
        return Lists.newArrayList(new AppModule());
    }

    //==========================================
    // The application
    //==========================================
    private final Server server;
    private final DefaultRouter router;
    private final TodoController todoController;

    /**
     * The application constructor which Guice will call
     * with the required dependencies.
     */
    @Inject
    public App(Server server,
               DefaultRouter router,
               TodoController todoController) {
        this.server = server;
        this.router = router;
        this.todoController = todoController;
    }

    protected Server getServer() {
        return this.server;
    }

    protected DefaultRouter getRouter() {
        return this.router;
    }

    protected TodoController getTodoController() {
        return this.todoController;
    }

    /**
     * Adds the routes and starts the server.
     */
    protected void start() {
        addRoutes();
        getServer().start();

        this.logger.info("Application started!");
    }

    /**
     * Application routes.
     */
    protected void addRoutes() {

        //==========================================
        // We enable Cors on all routes
        //==========================================
        getRouter().cors();

        getRouter().GET("/").save(getTodoController()::getTodosHandler);
        getRouter().POST("/").save(getTodoController()::addTodoHandler);
        getRouter().DELETE("/").save(getTodoController()::deleteTodosHandler);

        getRouter().GET("/${todoId:<N>}").save(getTodoController()::getTodoHandler);
        getRouter().PATCH("/${todoId:<N>}").save(getTodoController()::patchTodoHandler);
        getRouter().DELETE("/${todoId:<N>}").save(getTodoController()::deleteTodoHandler);
    }

}
