package edu.csulb;

import java.util.ArrayList;

public class datapointCollecter {


    private static datapointCollecter instance = null;
    private ArrayList<Double> defaultPrecisions = new ArrayList<Double>();
    private ArrayList<Double> defaultRecalls = new ArrayList<Double>();
    private ArrayList<Double> tfidfPrecisions = new ArrayList<Double>();
    private ArrayList<Double> tfidfRecalls = new ArrayList<Double>();
    private ArrayList<Double> okapiPrecisions = new ArrayList<Double>();
    private ArrayList<Double> okapiRecalls = new ArrayList<Double>();
    private ArrayList<Double> wackyPrecisions = new ArrayList<Double>();
    private ArrayList<Double> wackyRecalls = new ArrayList<Double>();

    private datapointCollecter() {


    }

    private datapointCollecter(ArrayList<Double> defaultPrecisions, ArrayList<Double> defaultRecalls, ArrayList<Double> tfidfPrecisions, ArrayList<Double> tfidfRecalls, ArrayList<Double> okapiPrecisions, ArrayList<Double> okapiRecalls, ArrayList<Double> wackyPrecisions, ArrayList<Double> wackyRecalls) {
        this.defaultPrecisions = defaultPrecisions;
        this.defaultRecalls = defaultRecalls;
        this.tfidfPrecisions = tfidfPrecisions;
        this.tfidfRecalls = tfidfRecalls;
        this.okapiPrecisions = okapiPrecisions;
        this.okapiRecalls = okapiRecalls;
        this.wackyPrecisions = wackyPrecisions;
        this.wackyRecalls = wackyRecalls;
    }

    public static datapointCollecter getInstance() {
        if (instance == null) {
            instance = new datapointCollecter();
        }
        return instance;
    }

    public static datapointCollecter getInstance(ArrayList<Double> defaultPrecisions, ArrayList<Double> defaultRecalls, ArrayList<Double> tfidfPrecisions, ArrayList<Double> tfidfRecalls, ArrayList<Double> okapiPrecisions, ArrayList<Double> okapiRecalls, ArrayList<Double> wackyPrecisions, ArrayList<Double> wackyRecalls) {
        if (instance == null) {
            instance = new datapointCollecter(defaultPrecisions, defaultRecalls, tfidfPrecisions, tfidfRecalls, okapiPrecisions, okapiRecalls, wackyPrecisions, wackyRecalls);
        }
        return instance;
    }

    public ArrayList<Double> getDefaultPrecisions() {
        return defaultPrecisions;
    }



    public  ArrayList<Double> getDefaultRecalls() {
        return defaultRecalls;
    }



    public  ArrayList<Double> getTfidfPrecisions() {
        return tfidfPrecisions;
    }



    public  ArrayList<Double> getTfidfRecalls() {
        return tfidfRecalls;
    }



    public  ArrayList<Double> getOkapiPrecisions() {
        return okapiPrecisions;
    }



    public  ArrayList<Double> getOkapiRecalls() {
        return okapiRecalls;
    }


    public  ArrayList<Double> getWackyPrecisions() {
        return wackyPrecisions;
    }



    public ArrayList<Double> getWackyRecalls() {
        return wackyRecalls;
    }

    



    
}
