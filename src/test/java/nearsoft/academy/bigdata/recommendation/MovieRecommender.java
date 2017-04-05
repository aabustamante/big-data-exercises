package nearsoft.academy.bigdata.recommendation;

import com.google.common.collect.HashBiMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.MemoryIDMigrator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.*;
import java.util.*;

/**
 * Created by alan on 3/04/17.
 */
public class MovieRecommender {
    private final String OUTPUT_FILE;
    private DataModel model;
    int total_reviews;

    HashBiMap<String, Long> users = HashBiMap.create();
    HashBiMap<String, Long> movies = HashBiMap.create();

    public MovieRecommender(String pathFile) throws IOException {
        OUTPUT_FILE = "moviesFormatted.txt";
        createFormattedFile(pathFile, OUTPUT_FILE);
        model = new FileDataModel(new File(OUTPUT_FILE));
    }

    private void createFormattedFile(String inputFileName, String outputFileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFileName));
        PrintStream fileStream = new PrintStream(outputFileName);

        String movie_indicator = "product/productId: ";
        String user_indicator = "review/userId: ";
        String score_indicator = "review/score: ";

        String movie_id = "";
        String user_id = "";
        String score_value = "";
        String result = "";

        String line;
        long cont_users;
        long cont_movies;
        while ((line = br.readLine())!=null) {
            if (line.contains(movie_indicator))movie_id = line.substring(movie_indicator.length());
            if (line.contains(user_indicator))user_id = line.substring(user_indicator.length());
            if (line.contains(score_indicator)) {

                if (!users.containsKey(user_id)){
                    users.put(user_id, (long)users.size());
                }
                cont_users = users.get(user_id);

                if (!movies.containsKey(movie_id)){
                    movies.put(movie_id, (long)movies.size());
                }
                cont_movies = movies.get(movie_id);

                score_value = line.substring(score_indicator.length());
                result = cont_users+","+cont_movies+","+score_value;
                fileStream.println(result);
                total_reviews++;
            }
        }
        fileStream.close();
    }

    public List<String> getRecommendationsForUser(String user) throws IOException, TasteException {

        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        List<RecommendedItem> recommendations = recommender.recommend(users.get(user), 3);
        List<String> result = new ArrayList<String>();

        for (RecommendedItem recommendation : recommendations) {
            result.add(movies.inverse().get(recommendation.getItemID()).toString());
        }
        return result;
    }

    public int getTotalReviews() throws IOException {
        return total_reviews;
    }

    public int getTotalProducts() throws TasteException {
        return model.getNumItems();
    }

    public int getTotalUsers() throws TasteException, IOException {
        return users.size();
    }
}