package app.routes;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public final class Routes {
    private Routes() {}

    /** Saml dine modulære ruter her. */
    public static EndpointGroup api() {
        return () -> {
            // Versioner gerne dine endpoints
            path("/v1", () -> {
                // SLET ELLER RETTE — kun et eksempel:
                get("/ping", ctx -> ctx.json("pong"));

                // TODO: Her kan du montere feature-moduler:
                // path("/users", UsersRoutes::register);
                // path("/orders", OrdersRoutes::register);
            });
        };
    }
}
