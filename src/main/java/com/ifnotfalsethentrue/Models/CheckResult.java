package com.ifnotfalsethentrue.models;

import java.util.ArrayList;

/**
 * Created by a.khilazhev on 23.01.2016.
 */
public class CheckResult {
    public ArrayList<String> result;

    public CheckResult(ArrayList<String> result) {
        this.result = result;
    }
    public CheckResult(String message) {
        result = new ArrayList<String>();
        result.add(message);
    }
}
