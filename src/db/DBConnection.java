package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBConnection {

    private static DBConnection dbConnection;
    private Connection connection;

    private DBConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library?createDatabaseIfNotExist=true&allowMultiQueries=true", "root", "1234");
            PreparedStatement pstm = connection.prepareStatement("SHOW TABLES");
            ResultSet resultSet = pstm.executeQuery();
            if (!resultSet.next()) {
                String sql = "\n" +
                        "CREATE TABLE `bookdetail` (\n" +
                        "  `id` varchar(10) NOT NULL,\n" +
                        "  `title` varchar(15) DEFAULT NULL,\n" +
                        "  `author` varchar(20) DEFAULT NULL,\n" +
                        "  `status` varchar(20) DEFAULT NULL,\n" +
                        "  PRIMARY KEY (`id`)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n" +
                        "\n" +
                        "CREATE TABLE `issuetb` (\n" +
                        "  `issueId` varchar(10) NOT NULL,\n" +
                        "  `date` date DEFAULT NULL,\n" +
                        "  `memberId` int(10) DEFAULT NULL,\n" +
                        "  `bookid` int(10) DEFAULT NULL,\n" +
                        "  PRIMARY KEY (`issueId`)\n" +
                        "  CONSTRAINT FOREIGN KEY (`memberId`) REFERENCES `memberdetail` (`id`),\n" +
                        "  CONSTRAINT FOREIGN KEY (`bookid`) REFERENCES `bookdetail` (`id`)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n" +
                        "\n" +
                        "CREATE TABLE `memberdetail` (\n" +
                        "  `id` int(11) NOT NULL,\n" +
                        "  `name` date DEFAULT NULL,\n" +
                        "  `address` varchar(50) DEFAULT NULL,\n" +
                        "  `contact` varchar(12) DEFAULT NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n" +
                        "\n" +
                        "CREATE TABLE `returndetail` (\n" +
                        "  `id` int(11) NOT NULL,\n" +
                        "  `issuedDate` date NOT NULL,\n" +
                        "  `returnedDate` date DEFAULT NULL,\n" +
                        "  `fine` int(10) DEFAULT NULL,\n" +
                        "  `issueid` int(10) DEFAULT NULL,\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  CONSTRAINT FOREIGN KEY (`issueid`) REFERENCES `issuetb` (`issueId`),\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n";
                pstm = connection.prepareStatement(sql);
                pstm.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static DBConnection getInstance() {
        return dbConnection = ((dbConnection == null) ? new DBConnection() : dbConnection);

    }

    public Connection getConnection() {
        return connection;
    }


}
