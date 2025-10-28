package app;

import app.config.ApplicationConfig;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        Javalin app = ApplicationConfig.start(7070);
        // Tilføj evt. shutdown-hooks eller metrics her.

    }
}

