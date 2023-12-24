package org.example;

import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
public class Schema {
    private StructType schema;
    public Schema() {
        this.schema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("id", DataTypes.LongType, true),
                DataTypes.createStructField("sym", DataTypes.StringType, true),
                DataTypes.createStructField("mc", DataTypes.StringType, true),
                DataTypes.createStructField("c", DataTypes.DoubleType, true),
                DataTypes.createStructField("f", DataTypes.DoubleType, true),
                DataTypes.createStructField("r", DataTypes.DoubleType, true),
                DataTypes.createStructField("lastPrice", DataTypes.DoubleType, true),
                DataTypes.createStructField("lastVolume", DataTypes.DoubleType, true),
                DataTypes.createStructField("lot", DataTypes.DoubleType, true),
                DataTypes.createStructField("ot", DataTypes.StringType, true),
                DataTypes.createStructField("changePc", DataTypes.StringType, true),
                DataTypes.createStructField("avePrice", DataTypes.StringType, true),
                DataTypes.createStructField("highPrice", DataTypes.StringType, true),
                DataTypes.createStructField("lowPrice", DataTypes.StringType, true),
                DataTypes.createStructField("fBVol", DataTypes.StringType, true),
                DataTypes.createStructField("fBValue", DataTypes.StringType, true),
                DataTypes.createStructField("fSVolume", DataTypes.StringType, true),
                DataTypes.createStructField("fSValue", DataTypes.StringType, true),
                DataTypes.createStructField("fRoom", DataTypes.StringType, true),
                DataTypes.createStructField("g1", DataTypes.StringType, true),
                DataTypes.createStructField("g2", DataTypes.StringType, true),
                DataTypes.createStructField("g3", DataTypes.StringType, true),
                DataTypes.createStructField("g4", DataTypes.StringType, true),
                DataTypes.createStructField("g5", DataTypes.StringType, true),
                DataTypes.createStructField("g6", DataTypes.StringType, true),
                DataTypes.createStructField("g7", DataTypes.StringType, true),
                DataTypes.createStructField("mp", DataTypes.StringType, true),
                DataTypes.createStructField("CWUnderlying", DataTypes.StringType, true),
                DataTypes.createStructField("CWIssuerName", DataTypes.StringType, true),
                DataTypes.createStructField("CWType", DataTypes.StringType, true),
                DataTypes.createStructField("CWMaturityDate", DataTypes.StringType, true),
                DataTypes.createStructField("CWLastTradingDate", DataTypes.StringType, true),
                DataTypes.createStructField("CWExcersisePrice", DataTypes.StringType, true),
                DataTypes.createStructField("CWExerciseRatio", DataTypes.StringType, true),
                DataTypes.createStructField("CWListedShare", DataTypes.StringType, true),
                DataTypes.createStructField("sType", DataTypes.StringType, true),
                DataTypes.createStructField("sBenefit", DataTypes.StringType, true),
                DataTypes.createStructField("industry", DataTypes.StringType, true),
                DataTypes.createStructField("crawledTime", DataTypes.TimestampType, true)}
        );
    }

    public StructType getSchema() {
        return schema;
    }
}
