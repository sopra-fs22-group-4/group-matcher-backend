package ch.uzh.soprafs22.groupmatcher.matching;


import ch.uzh.soprafs22.groupmatcher.service.MatcherService;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomUniformGeneratedInitialMeans;
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
import tutorial.clustering.SameSizeKMeansAlgorithm;

import java.util.List;

import ch.uzh.soprafs22.groupmatcher.service.MatcherService.*;

public class ClusteringModel {

    private MatcherService matcherService;

    public Clustering<MeanModel> initializeModel(Long matcherId)
    {
        double[][] answerMatrix = matcherService.createAnswerMatrixAll(matcherId);
        String[] studentEmails = matcherService.getStudentEmails(matcherId);

        DatabaseConnection databaseConnection = new ArrayAdapterDatabaseConnection(answerMatrix, studentEmails);
        Database database = new StaticArrayDatabase(databaseConnection, null);
        database.initialize();
        Relation<NumberVector> relation = database.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);

        RandomUniformGeneratedInitialMeans randomizer = new RandomUniformGeneratedInitialMeans(RandomFactory.DEFAULT);
        SameSizeKMeansAlgorithm<NumberVector> model = new SameSizeKMeansAlgorithm<>(EuclideanDistanceFunction.STATIC, 3, -1, randomizer);

        Clustering<MeanModel> results = model.run(database, relation);

        return results;
    }

}
