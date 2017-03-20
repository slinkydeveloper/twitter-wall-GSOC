package twitterwallservice;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * The service interface.
 */
@ProxyGen // Generate service proxies
@VertxGen // Generate the clients
public interface TwitterUpdaterInterface {

    static TwitterUpdaterInterface create(Vertx vertx) {
        return new TwitterUpdaterInterfaceImpl(vertx);
    }

    static TwitterUpdaterInterface createProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(TwitterUpdaterInterface.class, vertx, address);
    }

    // The service methods

    void updateTwitter(String hashtag, Handler<AsyncResult<JsonObject>> resultHandler);

}