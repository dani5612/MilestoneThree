package edu.csulb;

import java.util.ArrayList;

import javafx.application.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.scene.Scene;


public class GraphMaker extends Application {




  
     @Override
     public void start(Stage stage){
        

         datapointCollecter datapoint = datapointCollecter.getInstance();

         ArrayList<Double> defaultPrecisions = new ArrayList<Double>();
         ArrayList<Double> defaultRecalls = new ArrayList<Double>();
         ArrayList<Double> tfidfPrecisions = new ArrayList<Double>();
         ArrayList<Double> tfidfRecalls = new ArrayList<Double>();
         ArrayList<Double> okapiPrecisions = new ArrayList<Double>();
         ArrayList<Double> okapiRecalls = new ArrayList<Double>();
         ArrayList<Double> wackyPrecisions = new ArrayList<Double>();
         ArrayList<Double> wackyRecalls = new ArrayList<Double>();

         defaultPrecisions = datapoint.getDefaultPrecisions();
         defaultRecalls = datapoint.getDefaultRecalls();
         tfidfPrecisions = datapoint.getTfidfPrecisions();
         tfidfRecalls = datapoint.getTfidfRecalls();
         okapiPrecisions = datapoint.getOkapiPrecisions();
         okapiRecalls = datapoint.getOkapiRecalls();
         wackyPrecisions = datapoint.getWackyPrecisions();
         wackyRecalls = datapoint.getWackyRecalls();

         stage.setTitle("RECALL - PRECISSION GRAPH");

         NumberAxis xAxis = new NumberAxis();
         xAxis.setLabel("Recall");

         NumberAxis yAxis = new NumberAxis();
         yAxis.setLabel("Precission");

         LineChart plane = new LineChart<>(xAxis, yAxis);

         plane.setTitle("RECALL - PRECISSION GRAPH");

         System.out.println("Problem?");

         XYChart.Series series1 = new XYChart.Series();
         series1.setName("default");
         XYChart.Series series2 = new XYChart.Series();
         series2.setName("tfidf");
         XYChart.Series series3 = new XYChart.Series();
         series3.setName("OKAPI");
         XYChart.Series series4 = new XYChart.Series();
         series4.setName("Wacky");

         for(int i =0; i < defaultPrecisions.size(); i++){
             series1.getData().add(new XYChart.Data(defaultRecalls.get(i), defaultPrecisions.get(i)));
         }

         for(int i =0; i < tfidfPrecisions.size(); i++){
             series2.getData().add(new XYChart.Data(tfidfRecalls.get(i), tfidfPrecisions.get(i)));
         }

         for(int i =0; i < okapiPrecisions.size(); i++){
             series3.getData().add(new XYChart.Data(okapiRecalls.get(i), okapiPrecisions.get(i)));
         }

         for(int i =0; i < wackyPrecisions.size(); i++){
             series4.getData().add(new XYChart.Data(wackyRecalls.get(i), wackyPrecisions.get(i)));
         }

         plane.getData().addAll(series1, series2, series3, series4);

         Scene scene = new Scene(plane, 800, 600);

         stage.setScene(scene);

         stage.show();





    
     }
    
 }
