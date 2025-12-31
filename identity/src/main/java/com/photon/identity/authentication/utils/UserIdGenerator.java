package com.photon.identity.authentication.utils;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        final String prefix = "U";
        int offset = 101;

        try {
            Connection connection = session.getJdbcConnectionAccess().obtainConnection();
            /* Correct SQL to get the next value from the sequence
             * Make sure the schema name is correct, or omit it if the sequence is in the default schema.
             * If your sequence is in the 'public' schema, use "SELECT nextval('user_id_sequence')"
             * */
            String sql = "SELECT nextval('user_id_sequence')";
            /* If your sequence is in a specific schema like 'photon_identity', use: */
            /* String sql = "SELECT nextval('photon_identity.user_id_sequence')"; */

            try (PreparedStatement ps = connection.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    long seqValue = rs.getLong(1);
                    long finalValue = seqValue + offset;
                    String generatedId = prefix + finalValue;
                    System.out.println("Generated ID: " + generatedId);
                    return generatedId;
                }
            }
        } catch (SQLException e) {
            throw new HibernateException("Unable to generate User ID from sequence", e);
        }
        throw new HibernateException("Unable to generate User ID: No sequence value obtained from nextval()");
    }
}