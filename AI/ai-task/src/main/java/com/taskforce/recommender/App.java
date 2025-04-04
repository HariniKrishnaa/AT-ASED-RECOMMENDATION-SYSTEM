package com.taskforce.recommender;

import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class App {
    public static void main(String[] args) {
        try {
            System.out.println("Loading dataset...");

            InputStream inputStream = App.class.getClassLoader().getResourceAsStream("data.csv");

            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: data.csv");
            }

            System.out.println("Dataset found. Copying to temp file...");

            File tempFile = File.createTempFile("data", ".csv");
            tempFile.deleteOnExit();
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Building data model...");
            DataModel model = new FileDataModel(tempFile);

            System.out.println("Calculating user similarity...");
            UserSimilarity similarity = new LogLikelihoodSimilarity(model);

            System.out.println("Finding nearest neighbors...");
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, model);

            System.out.println("Building recommender...");
            Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

            System.out.println("User 1 preferences:");
            PreferenceArray prefs = model.getPreferencesFromUser(1);
            for (int i = 0; i < prefs.length(); i++) {
                System.out.println("Item: " + prefs.getItemID(i) + ", Rating: " + prefs.getValue(i));
            }

            System.out.println("Generating recommendations for user 1...");
            List<RecommendedItem> recommendations = recommender.recommend(1, 3);

            if (recommendations.isEmpty()) {
                System.out.println("No recommendations found for user 1.");
                System.out.println("Trying another user (user 2) for testing...");
                recommendations = recommender.recommend(2, 3);
                if (recommendations.isEmpty()) {
                    System.out.println("Still no recommendations found.");
                } else {
                    System.out.println("Recommendations for user 2:");
                    for (RecommendedItem item : recommendations) {
                        System.out.println("Item ID: " + item.getItemID() + ", Score: " + item.getValue());
                    }
                }
            } else {
                System.out.println("Recommendations for user 1:");
                for (RecommendedItem item : recommendations) {
                    System.out.println("Item ID: " + item.getItemID() + ", Score: " + item.getValue());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}