package org.persistence;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.jboss.logging.Logger;

@RequestScoped
@Transactional
public class DetectionService {

    private static final Logger LOGGER = Logger.getLogger(NumberService.class.getName());

    @Inject
    EntityManager entityManager;

    @Transactional
    public void insertNewDetection(TrackingOutput to) {

        Timestamp insertionTimestamp = new Timestamp(System.currentTimeMillis());
        String table = config.getProperty("db.hypertable");

        String query = "INSERT INTO \"" + table + "\" ";
        query += "(\"CAPTURE_TS\", \"CLASS_ID\", \"CONFIDENCE\", \"OBJECT_ID\", \"MIN_X\", \"MIN_Y\",\"MAX_X\",\"MAX_Y\") ";
        query += "VALUES (?,?,?,?,?,?,?,?)";
        
        PreparedStatement preStmt;
        try {
            preStmt = conn.prepareStatement(query);
        } catch (SQLException e) {
            log.warn("creation of prepared statement didn't work " + e.getMessage());
            return;
        }
        
        Timestamp captureTimestamp = new Timestamp(to.getFrame().getTimestampUtcMs());
        
        List<TrackedDetection> list = to.getTrackedDetectionsList();
        for (TrackedDetection td : list) {
            int classId = td.getDetection().getClassId();
            float confidence = td.getDetection().getConfidence();
            byte[] objectID = td.getObjectId().toByteArray();
            HexFormat hex = HexFormat.of();
            String oId = hex.formatHex(objectID);
            int minX = td.getDetection().getBoundingBox().getMinX();
            int minY = td.getDetection().getBoundingBox().getMinY();
            int maxX = td.getDetection().getBoundingBox().getMaxX();
            int maxY = td.getDetection().getBoundingBox().getMaxY();

            try {

                preStmt.setTimestamp(1, captureTimestamp, null);
                preStmt.setInt(2, classId);
                preStmt.setFloat(3, confidence);
                preStmt.setString(4, oId);
                preStmt.setInt(5, minX);
                preStmt.setInt(6, minY);
                preStmt.setInt(7, maxX);
                preStmt.setInt(8, maxY);
                preStmt.addBatch();
            }  catch (SQLException e) {
                log.warn("Adding insert to prepStmt didn't work " + e.getMessage());
            }
            log.info("created batched prep stmt");
        }

        try {
            preStmt.executeBatch();
            log.info("inserted new data");
        } catch (SQLException e) {
            log.warn("executing bached prepared stmt didn't work " + e.getMessage());
        }
    }
}