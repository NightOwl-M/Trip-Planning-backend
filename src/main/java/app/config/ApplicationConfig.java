package app.config;

import app.exceptions.ErrorHandlers;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApplicationConfig {

    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    private ApplicationConfig() {}

    public static Javalin create() {
        // Alt hvad der kan sættes via cfg -> herunder contextPath i v6
        Javalin app = Javalin.create((JavalinConfig cfg) -> {
            cfg.http.defaultContentType = "application/json";
            cfg.router.contextPath = "/api";              // ← v6-korrekt
            // Ingen cfg.plugins her (for at undgå versions-API forskelle)
        });

        // MANUEL CORS (stabilt på tværs af versioner)
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Authorization, Content-Type");
        });
        app.options("/*", ctx -> ctx.status(204));

        // Simpel timing/log
        app.before(ctx -> ctx.attribute("startNs", System.nanoTime()));
        app.after(ctx -> {
            Long startNs = ctx.attribute("startNs");
            long ms = (startNs == null) ? -1 : (System.nanoTime() - startNs) / 1_000_000;
            log.info("{} {} -> {} ({} ms)", ctx.method(), ctx.path(), ctx.status(), ms);
        });

        return app;
    }

    public static Javalin start(int port) {
        Javalin app = create();
        ErrorHandlers.register(app);

        // Health (→ /api/health)
        app.get("/health", ctx -> ctx.json(java.util.Map.of("status", "ok")));

        // Tilføj DINE ruter her, når du er klar:
        // app.routes(app.routes.Routes.api());

        app.start(port);
        return app;
    }

    public static void stop(Javalin app) {
        if (app != null) app.stop();
    }
}
