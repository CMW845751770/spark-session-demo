package cn.edu.tju;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: CMW天下第一
 * @Date: 2020/5/25 21:51
 */
public class Main {


    public static void main(String[] args) {
        SparkSession session = SparkSession.builder().master("local").appName("corporation-culture").getOrCreate();
        session.sparkContext().setLogLevel("ERROR");
        JavaRDD<String> stringJavaRDD = session.read().textFile("input/America.txt").toJavaRDD();
        JavaRDD<String> flatMap = stringJavaRDD.flatMap(line -> {
            JiebaSegmenter jiebaSegmenter = new JiebaSegmenter();
            List<SegToken> segTokens = jiebaSegmenter.process(line, JiebaSegmenter.SegMode.INDEX);
            return segTokens.stream().map(token ->{
                    return token.word ;
            }).collect(Collectors.toList()).iterator() ;
        } );
        JavaPairRDD<String, Integer> javaPairRDD = flatMap.mapToPair(word -> new Tuple2(word, 1));
        JavaPairRDD<String, Integer> resultRDD = javaPairRDD.reduceByKey((v1, v2) -> v1 + v2);
        JavaPairRDD<Integer, String> ans = resultRDD.mapToPair((row) -> new Tuple2<>(row._2, row._1))
                .sortByKey(false);
        ans.foreach(t->{
            System.out.println(t._1+"----->"+t._2);
        });
        session.close();
    }
}
