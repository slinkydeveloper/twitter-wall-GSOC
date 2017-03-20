package twitterwallservice;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created byfrancesco on 20/03/17.
 */
public class TwitterUpdaterInterfaceImpl implements TwitterUpdaterInterface {

    TwitterHTTPClient twitterHTTPClient;

    public TwitterUpdaterInterfaceImpl(Vertx vertx) {
        twitterHTTPClient = new TwitterHTTPClient(vertx);
    }

    @Override
    public void updateTwitter(String hashtag, Handler<AsyncResult<JsonObject>> resultHandler) {
        System.out.println("Requested twitter wall update with hashtag: " + hashtag);
        twitterHTTPClient.updateTwitterWall(hashtag, resultHandler);
    }
}
