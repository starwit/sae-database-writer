package de.starwit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SqlScriptExecutor {
    private final static Logger log = LogManager.getLogger(SqlScriptExecutor.class);
    private static final Pattern COMMENT_PATTERN = Pattern.compile("--.*|/\\*(.|[\\r\\n])*?\\*/");

    public static void executeBatchedSQL(String scriptFilePath, Connection connection, int batchSize) throws Exception {
        List<String> sqlStatements = parseSQLScript(scriptFilePath);
        executeSQLBatches(connection, sqlStatements, batchSize);
    }

    private static List<String> parseSQLScript(String scriptFilePath) throws IOException {
        List<String> sqlStatements = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(scriptFilePath))) {
            StringBuilder currentStatement = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher commentMatcher = COMMENT_PATTERN.matcher(line);
                line = commentMatcher.replaceAll("");
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                currentStatement.append(line).append(" ");

                if (line.endsWith(";")) {
                    sqlStatements.add(currentStatement.toString());
                    log.info(currentStatement.toString());
                    currentStatement.setLength(0);
                }
            }
        } catch (IOException e) {
            log.error("Couldn't read sql file", e);
            throw e;
        }
        return sqlStatements;
    }

    private static void executeSQLBatches(Connection connection, List<String> sqlStatements, int batchSize)
            throws SQLException {
        int count = 0;
        Statement statement = connection.createStatement();

        for (String sql : sqlStatements) {
            statement.addBatch(sql);
            count++;

            if (count % batchSize == 0) {
                log.info("Executing batch");
                statement.executeBatch();
                statement.clearBatch();
            }
        }
        // Execute any remaining statements
        if (count % batchSize != 0) {
            statement.executeBatch();
        }
        connection.commit();
    }
}