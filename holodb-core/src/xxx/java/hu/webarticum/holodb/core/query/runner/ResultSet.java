package hu.webarticum.holodb.core.query.runner;

import java.math.BigInteger;

public interface ResultSet extends Iterable<ResultRecord> {

   public BigInteger size();
    
}
