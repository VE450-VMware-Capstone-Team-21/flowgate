package com.example.flowgateapiapp;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * An interface used to determine what to do when receiving asset info
 */
public interface flowgateClientActor {
    public void act(JSONObject response) throws JSONException;
}
