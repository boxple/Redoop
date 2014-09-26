package com.boxple.redoop;

import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class RedisReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context output)
            throws IOException, InterruptedException {
        
    	int keyCount = 0;
        
        for(IntWritable value: values){
        	keyCount+= value.get();
        }
        output.write(key, new IntWritable(keyCount));
    }
}
