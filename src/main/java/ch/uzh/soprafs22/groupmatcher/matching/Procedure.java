package ch.uzh.soprafs22.groupmatcher.matching;

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomUniformGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.MeanModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.utilities.random.RandomFactory;
import lombok.extern.slf4j.Slf4j;
import tutorial.clustering.SameSizeKMeansAlgorithm;

import java.util.stream.IntStream;

@Slf4j
public class Procedure {

    public static void main(String[] args) {


        double[][] answersMatrix = new double[][] {
                { 0,0,0,4, 6,0,0,0,0,0 },
                { 0,4,0,0, 0,6,0,0,0,0 },
                { 4,0,0,0, 0,6,0,0,0,0 },
                { 0,4,0,0, 0,0,6,0,0,0 },
                { 0,4,0,0, 0,0,0,0,0,6 },
                { 0,4,0,0, 0,0,0,0,0,6 },
                { 4,0,0,0, 0,0,0,0,0,6 } };
        
        String[] studentsNames = new String[]{"student1", "student2", "student3", "student4", "student5", "student6", "student7"};
        DatabaseConnection databaseConnection = new ArrayAdapterDatabaseConnection(answersMatrix, studentsNames);
        Database database = new StaticArrayDatabase(databaseConnection, null);
        database.initialize();
        Relation<NumberVector> relation = database.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

        RandomUniformGeneratedInitialMeans randomizer = new RandomUniformGeneratedInitialMeans(RandomFactory.DEFAULT);
        SameSizeKMeansAlgorithm<NumberVector> model = new SameSizeKMeansAlgorithm<>(EuclideanDistanceFunction.STATIC, 3, -1, randomizer);
        Clustering<MeanModel> results = model.run(database, relation);


        log.info("Group size: " + results.getAllClusters().size());
        IntStream.range(0, results.getAllClusters().size()).forEach(index -> {
            Cluster<MeanModel> cluster = results.getAllClusters().get(index);
            log.info("Index: {}, Name: {}, Auto Name: {}, Mean: {}", index, cluster.getName(), cluster.getNameAutomatic(), cluster.getModel().getMean());
            cluster.getIDs().forEach(id -> log.info("Student: {}", id.internalGetIndex()));
        });
    }
}
