import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithms.binder.*;
import de.metanome.algorithms.tane.TaneAlgorithm;
import de.metanome.backend.input.database.DefaultTableInputGenerator;
import de.metanome.backend.input.file.DefaultFileInputGenerator;
import org.bson.Document;

import java.io.File;
import java.util.ArrayList;

// Mongo Imports

public class TestMetanomeV2
{
    // Mongo
    private com.mongodb.client.MongoDatabase movieDB = null;
    private MongoClient mongoClient = null;
    private MongoCollection<Document> collection = null;
    private final String IP_ADDRESS = "localhost";
    private final int PORT_NUMBER = 27017;
    private File folder = new File("C:/Users/jnamb/Desktop/1st Year PhD/Fall/CSCI-729/Project/src/main/java/collections");
    private ArrayList<DefaultFileInputGenerator> fileList = new ArrayList<>();


    public static void main(String [] args)
    {
        new TestMetanomeV2();
    }//end main

    public TestMetanomeV2()
    {
        try {
            // Access database
            connect();
            if(this.movieDB != null) {
                System.out.println("Connection established!");

                // Get the name of all the collections
                MongoIterable<String> collectionList = this.movieDB.listCollectionNames();

                // Instantiate a printing dependency receiver
                PrintingDependencyReceiver receiver = new PrintingDependencyReceiver();

                // Loop through list of collections
                for (String collectionName : collectionList) {
                    collectionName = folder + "\\" + collectionName;
                    ConfigurationSettingFileInput config = new ConfigurationSettingFileInput(collectionName);
                    DefaultFileInputGenerator line = new DefaultFileInputGenerator(config);
                    fileList.add(line);

                    // Run Tane algorithm
                    TaneAlgorithm tane = new TaneAlgorithm();
                    tane.setRelationalInputConfigurationValue(TaneAlgorithm.INPUT_TAG, line);
                    tane.setResultReceiver(receiver);
                    tane.execute();
                }//end for loop

                // Make a Binder File
                BINDERFile binder = new BINDERFile();

                // Create array of default file input generator
                DefaultFileInputGenerator[] fileArray = fileList.toArray(new DefaultFileInputGenerator[fileList.size()]);

                // Call binder methods
                binder.setRelationalInputConfigurationValue(BINDERFile.Identifier.INPUT_FILES.name(), fileArray);
                binder.setBooleanConfigurationValue(BINDERFile.Identifier.DETECT_NARY.name(), true);
                binder.setResultReceiver(receiver);
                binder.execute();
                receiver.output();

                // Infer dependencies
                receiver.infer();

                // BCNF decomposition
                receiver.bcnfDecompose();

                // Fold
                receiver.fold();

            }
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }//end constructor

    /**
     * Connect to the database
     */
    public void connect()
    {
        try {
            // Create a Mongo client
            mongoClient = new MongoClient(IP_ADDRESS, PORT_NUMBER);
            // Access the database
            this.movieDB = mongoClient.getDatabase("movieReviewsDB");
        }
        catch(Exception e){
            System.out.println("ERROR connecting to server\n");
            System.out.println(e.getMessage());
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }//end connectToDatabase

}//end class
