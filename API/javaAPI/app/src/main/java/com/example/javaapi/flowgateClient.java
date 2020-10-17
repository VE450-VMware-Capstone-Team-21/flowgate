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

    // @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getAuthToken() throws IOException, JSONException {
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
        try{
            OutputStream os = tokenHttpCon.getOutputStream();
            os.write(inputUser, 0, inputUser.length);
        }
        catch (Exception e){
            Log.w("flowgateClient", "Fail to connect when asking for token");
            return null;
        }

        // receive and set this.token and return string if success
        tokenHttpCon.connect();
        int responseStatus = tokenHttpCon.getResponseCode();
        if(responseStatus == 200){
            InputStreamReader isr = new InputStreamReader(tokenHttpCon.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            StringBuilder response = new StringBuilder();
            String responseLine;
            while((responseLine = br.readLine()) != null){
                response.append(responseLine.trim());
            }
            this.token = new JSONObject(response.toString());
            return token.getString("access_token");
        }

        return null;
    }
}
