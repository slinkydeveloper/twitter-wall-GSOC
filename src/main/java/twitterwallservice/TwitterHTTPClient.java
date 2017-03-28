package twitterwallservice;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.BufferFactory;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.http.HttpClientOptions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;

/**
 * Created by francesco on 20/03/17.
 */
public class TwitterHTTPClient {

    private String access_token = null;
    private String api_key;
    private String api_secret;
    private WebClient client;
    Vertx vertx;

    public TwitterHTTPClient(Vertx vertx) {
        this.vertx = vertx;
        JsonObject config = Vertx.currentContext().config();
        this.api_key = config.getString("api_key");
        this.api_secret = config.getString("api_secret");
        HttpClientOptions options = new HttpClientOptions().
                setProtocolVersion(HttpVersion.HTTP_1_1).
                setSsl(true).
                setUseAlpn(true).
                setTrustAll(true).setDefaultHost("api.twitter.com").setDefaultPort(443);
        HttpClient httpClient = vertx.createHttpClient(options);
        client = WebClient.wrap(httpClient);
    }

    public void updateAccessToken(Handler<AsyncResult<Boolean>> next) {
        MultiMap form = MultiMap.caseInsensitiveMultiMap();
        form.set("grant_type", "client_credentials");

        String authorizationHeaderEncoded = "Basic " + Base64.getEncoder().encodeToString(new String(api_key + ":" + api_secret).getBytes());
        System.out.println("Starting updateAccessToken");

        client.post("/oauth2/token").putHeader("Authorization", authorizationHeaderEncoded).sendForm(form, result -> {
            if (result.succeeded()) {
                HttpResponse<Buffer> response = result.result();

                if (response.statusCode() != 200) {
                    System.out.println("Error during request update access token.Status code: " + response.statusCode() + " " + response.statusMessage() + " Response: " + response.bodyAsString());
                    next.handle(Future.succeededFuture(false));
                } else {
                    access_token = response.bodyAsJsonObject().getString("access_token");
                    System.out.println("Token successfully updated");
                    next.handle(Future.succeededFuture(true));
                }
            }
        });
    }

    private void internalUpdate(String search, Handler<AsyncResult<JsonObject>> next, String since_id, boolean retryWhenTokenExpired) {
        try {
            String url = "/1.1/search/tweets.json?q=" + URLEncoder.encode(search, "UTF-8") + ((since_id != null && since_id.length() != 0) ? ("&since_id=" + since_id) : "");
            System.out.println("Requesting at URL "+ url);
            client.get(url).putHeader("Authorization", "Bearer " + access_token).send(result -> {
                if (result.succeeded()) {
                    HttpResponse<Buffer> response = result.result();
                    if (response.statusCode() == 419) {
                        System.err.println("Too many requests!");
                        next.handle(Future.failedFuture("Too many requests!"));
                    } else if (response.statusCode() == 401 && retryWhenTokenExpired) {
                        System.out.println("Token expired! Trying to acquire new token");
                        this.updateAccessToken(updateResult -> {
                            if (updateResult.succeeded() && updateResult.result() == false) {
                                System.err.println("Awesome developer error during updateAccessToken");
                            } else {
                                internalUpdate(search, next, since_id, false);
                            }
                        });
                        internalUpdate(search, next, since_id, false);
                    } else {
                        System.out.println("Request done");
                        next.handle(Future.succeededFuture(response.bodyAsJsonObject()));
                    }
                } else {
                    System.out.println("Something went wrong " + result.cause().getMessage());
                    next.handle(Future.failedFuture("Something went wrong " + result.cause().getMessage()));
                }
            });
        } catch (UnsupportedEncodingException e) {
        System.err.println("Awesome developer error " + e);
        }
    }

    public void updateTwitterWall(String search, String since_id, Handler<AsyncResult<JsonObject>> next) {
        if (access_token == null) {
            System.out.println("No access_token");
                // Some hardcoded stuff :)
                // If access_token is null, retrieve the access_token and try to make a request
                this.updateAccessToken(updateResult -> {
                    if (updateResult.succeeded() && updateResult.result() == false) {
                        System.err.println("Awesome developer error during updateAccessToken");
                    } else {
                        internalUpdate(search, next, since_id, false);
                    }
                });
            } else {
                // There is an access_token, trying to make a request
                internalUpdate(search, next, since_id, true);
            }
    }

}
