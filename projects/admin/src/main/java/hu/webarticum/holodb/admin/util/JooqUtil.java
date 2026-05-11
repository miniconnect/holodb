package hu.webarticum.holodb.admin.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Date;

import org.jooq.DataType;
import org.jooq.impl.SQLDataType;

import hu.webarticum.minibase.storage.api.Column;
import hu.webarticum.miniconnect.lang.ByteString;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class JooqUtil {

    private JooqUtil() {
        // utility class
    }

    public static DataType<?> toDataType(Column column) {
        Class<?> clazz = column.definition().clazz();
        if (clazz == String.class) {
            if (column.possibleValues().isPresent()) {
                return SQLDataType.VARCHAR(255);
            } 
            return SQLDataType.CLOB;
        } else if (clazz == Character.class) {
            return SQLDataType.VARCHAR(1);
        } else if (clazz == ByteString.class) {
            return SQLDataType.BINARY;
        } else if (clazz == Boolean.class) {
            return SQLDataType.BOOLEAN;
        } else if (clazz == Byte.class) {
            return SQLDataType.TINYINT;
        } else if (clazz == Short.class) {
            return SQLDataType.SMALLINT;
        } else if (clazz == Integer.class) {
            return SQLDataType.INTEGER;
        } else if (clazz == Long.class || clazz == LargeInteger.class || clazz == BigInteger.class) {
            return SQLDataType.BIGINT;
        } else if (clazz == BigDecimal.class) {
            return SQLDataType.NUMERIC;
        } else if (clazz == Float.class) {
            return SQLDataType.REAL;
        } else if (clazz == Double.class) {
            return SQLDataType.DOUBLE;
        } else if (clazz == Date.class) {
            return SQLDataType.DATE;
        } else if (clazz == Time.class) {
            return SQLDataType.TIME;
        } else if (clazz == Timestamp.class) {
            return SQLDataType.TIMESTAMP;
        } else if (clazz == LocalDate.class) {
            return SQLDataType.LOCALDATE;
        } else if (clazz == LocalTime.class) {
            return SQLDataType.LOCALTIME;
        } else if (clazz == LocalDateTime.class) {
            return SQLDataType.LOCALDATETIME;
        } else if (clazz == OffsetTime.class) {
            return SQLDataType.OFFSETTIME;
        } else if (clazz == OffsetDateTime.class) {
            return SQLDataType.OFFSETDATETIME;
        } else if (clazz == Instant.class) {
            return SQLDataType.INSTANT;
        } else if (clazz == byte[].class) {
            return SQLDataType.BLOB;
        } else {
            return SQLDataType.OTHER;
        }
    }

    public static Object normalizeValue(Object value) {
        if (value instanceof LargeInteger largeIntegerValue) {
            return largeIntegerValue.bigIntegerValue();
        } else if (value instanceof ByteString byteStringValue) {
            return byteStringValue.extract();
        } else {
            return value;
        }
    }

}
