import org.json.simple.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.io.IOException;

public class EurekaRegistration {

    public static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    /**
     * This methods registers encryption manager to eureka.
     */
    public static void registerToEureka() {

        JSONObject port = new JSONObject();
        JSONObject dataCenterInfo = new JSONObject();
        JSONObject instance = new JSONObject();
        JSONObject data = new JSONObject();


        port.put("$", "9007");
        port.put("@enabled", "true");
        
        dataCenterInfo.put("@class", "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo");
        dataCenterInfo.put("name", "MyOwn");
      
        instance.put("hostName", "MICRONAUT-EUREKA-CLIENT");
        instance.put("app", "MICRONAUT-EUREKA-CLIENT");
        instance.put("ipAddr","127.0.0.1");
        instance.put("port", port);
        instance.put("dataCenterInfo", dataCenterInfo);
       
        data.put("instance", instance);


        if (checkIfEurekaIsUp("http://localhost:8761/eureka/apps") == true) {
            int response = postHttpRequest("http://localhost:8761/eureka/apps/MICRONAUT-EUREKA-CLIENT", data.toString());
            if (response == 204) {
                System.out.println("MICRONAUT-EUREKA-CLIENT successfully register to Eureka : " + response);
                sendHeartBeats();
            } else {
                System.out.println("Unable to register MICRONAUT-EUREKA-CLIENT to Eureka : " + response);
            }
        }
    }


    /**
     * This method performs http post JSON operation.
     * 
     * @param url
     * @param data
     * @return int
     */
    public static int postHttpRequest(String url, String data) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(data.toString())).build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println(url + " Connection refused");
        }
        return response.statusCode();
    }

    /**
     * This method performs http put operation.
     * 
     * @param url
     * @param data
     * @return int
     */
    public static int putHttpRequest(String url) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println(url + " Connection refused");
        }
        return response.statusCode();
    }

    /**
     * This method will start polling to eureka server.
     */
    public static void sendHeartBeats() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    putHttpRequest("http://localhost:8761/eureka/apps/MICRONAUT-EUREKA-CLIENT/MICRONAUT-EUREKA-CLIENT");
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException ie) {
                        System.out.println("Connection refused");
                    }
                }
            }
        }).start();
    }


    /**
     * This method will check if eureka service is up.
     * 
     * @param url
     */
    public static boolean checkIfEurekaIsUp(String url) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println(url + " Connection refused");
        }
        if (response.statusCode() == 200) {
            return true;
        }
        return false;
    }

}
