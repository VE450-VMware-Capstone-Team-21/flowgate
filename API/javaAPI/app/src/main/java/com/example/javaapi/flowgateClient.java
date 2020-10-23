package com.example.javaapi;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import org.json.*;

import javax.net.ssl.*;

public class flowgateClient {
    private String host;
    private String userName;
    private String password;

    private final String assetString = "/apiservice/v1/assets/";

    private JSONObject token = null;

    public flowgateClient(String host, String userName, String password){
        this.host = host;
        this.userName = userName;
        this.password = password;
    }

    public void setHost(String newHost){
        this.host = newHost;
    }

    public void setUserName(String newUserName){
        this.userName = newUserName;
    }

    public void setPassword(String newPassword){
        this.password = newPassword;
    }

    TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
    };

    // read `response` from `connection` after 200 OK
    private StringBuilder readResponse(HttpURLConnection connection) throws IOException {
        InputStreamReader isr = new InputStreamReader(connection.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        StringBuilder response = new StringBuilder();
        String responseLine;
        while((responseLine = br.readLine()) != null){
            response.append(responseLine.trim());
        }
        return response;
    }

    // @RequiresApi(api = Build.VERSION_CODES.KITKAT) throws IOException, JSONException
    public String getAuthToken() {
        try{
            /*
             * Existed token not expired
             */
            if(this.token != null){
                int expiresTime = Integer.parseInt(token.getString("expires_in"));
                long currentTime = System.currentTimeMillis(); // unix time in milliseconds
                if(expiresTime - currentTime > 600000){
                    return token.getString("access_token");
                }
            }

            /*
             * Acquire new token
             */
            // install all-trusting trust manager TODO because our server has no certificate
            try{
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            }
            catch (Exception e){}
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession sslSession) {
                    return true;
                }
            });

            // set up connection
            String authString = "/apiservice/v1/auth/token";
            URL tokenUrl = new URL("https://" + host + authString);
            HttpURLConnection tokenHttpCon = (HttpURLConnection)(tokenUrl.openConnection());
            tokenHttpCon.setRequestMethod("POST");
            tokenHttpCon.setDoOutput(true);
            tokenHttpCon.setRequestProperty("Content-Type", "application/json");
            tokenHttpCon.setRequestProperty("Accept", "application/json");

            // send authentication info
            String jsonUser = "{\"userName\": \""+(this.userName)+"\", "
                    + "\"password\": \""+this.password+"\"}";
            byte[] inputUser = jsonUser.getBytes(); // StandardCharsets.UTF_8
            OutputStream os = tokenHttpCon.getOutputStream();
            os.write(inputUser, 0, inputUser.length);

            // receive and set this.token and return string if success
            tokenHttpCon.connect();
            int responseStatus = tokenHttpCon.getResponseCode();
            if(responseStatus == 200){
                StringBuilder response = this.readResponse(tokenHttpCon);
                this.token = new JSONObject(response.toString());
                return token.getString("access_token");
            }

            return null;
        }
        catch (IOException e){
            Log.w("flowgateClient", "getAuthToken: IO exception when asking for token");
            return null;
        }
        catch (JSONException e){
            Log.w("flowgateClient", "getAuthToken: JSON exception when asking for token");
            return null;
        }
    }

    public JSONObject getAssetByName(String name){
        try{
            /*
             * Get token
             */
            String curToken = getAuthToken();
            if(curToken == null || curToken.isEmpty()){
                Log.w("flowgateClient", "getAssetByName: no available token");
                return null;
            }

            /*
             * Set up connection
             */
            String assetNameString = "/apiservice/v1/assets/name/";
            String assetNameUrlString = "https://" + this.host + assetNameString + name + "/";
            URL assetUrl = new URL(assetNameUrlString);
            HttpURLConnection assetHttpCon = (HttpURLConnection)(assetUrl.openConnection());
            assetHttpCon.setRequestMethod("GET");
            assetHttpCon.setDoOutput(true);
            assetHttpCon.setRequestProperty("Content-Type", "application/json");
            assetHttpCon.setRequestProperty("Authorization", "Bearer " + curToken);
            assetHttpCon.setRequestProperty("Accept", "application/json");

            Log.i("flowgateClient", "getAssetByName: query device: " + name);
            assetHttpCon.connect();
            int responseStatus = assetHttpCon.getResponseCode();
            if(responseStatus == 200){
                StringBuilder response = readResponse(assetHttpCon);
                return new JSONObject(response.toString());
            }
            else{
                Log.w("flowgateClient", "getAssetByName: response code not 200");
                return null;
            }
        }
        catch (IOException e){
            Log.w("flowgateClient", "getAssetByName: IO exception when asking for token");
            return null;
        }
        catch (JSONException e){
            Log.w("flowgateClient", "getAssetByName: JSON exception when asking for token");
            return null;
        }
    }
}
