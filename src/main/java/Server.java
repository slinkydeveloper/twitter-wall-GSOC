import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import twitterwallservice.TwitterUpdaterInterfaceImpl;
import io.vertx.serviceproxy.ProxyHelper;
import twitterwallservice.TwitterUpdaterInterface;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.CorsHandler;

public class Server extends AbstractVerticle {

    @Override
    public void start() throws Exception {

        TwitterUpdaterInterfaceImpl twitterUpdater = new TwitterUpdaterInterfaceImpl(vertx);

        ProxyHelper.registerService(TwitterUpdaterInterface.class, vertx, twitterUpdater, "twitterupdater");

        Router router = Router.router(vertx);

        router.routeWithRegex("^((?!\\/eventbus\\/).)*$").handler(StaticHandler.create());

        // CORS stuff
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedHeader("Access-Control-Request-Method")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Content-Type"));
        router.route().handler(BodyHandler.create());

        BridgeOptions opts = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress("twitterupdater"))
                .addOutboundPermitted(new PermittedOptions().setAddress("twitterupdater"));

        SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
        router.route("/eventbus/*").handler(ebHandler);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }
}