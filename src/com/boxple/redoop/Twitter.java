package com.boxple.redoop;

import java.io.*;
import java.util.*;
import java.text.*;
import java.util.Date;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class Twitter {
	
	// MAPPER
	public static class TwitMapper extends Mapper<LongWritable, Text, DateWordPair, IntWritable>{
	
		private JSONParser parser = new JSONParser();		
		private Text wordTxt = new Text();
		private IntWritable one = new IntWritable(1);
		private DateKey dateKey = new DateKey();
		private DateWordPair dateWordPair = new DateWordPair();
		//private Text dateWordPair = new Text();
		StringTokenizer itr;

		//public void map(LongWritable key, Text value, OutputCollector<DateWordPair, IntWritable> output)
		@Override
		public void map(LongWritable key, Text value, Context context) 
				throws IOException, InterruptedException {
			
			try{
				JSONObject twit = (JSONObject) parser.parse(value.toString());

				//String tweetId = twit.get("id").toString();
				String msgStr = twit.get("text").toString();
				//String oriTweetId = twit.optString("oriTweetId", null);
				String dateStr = twit.get("date").toString();
				//String userId = twit.get("user").toString();

				// {"id":-1000001754,"text":"&lt;손바닥 삼국지2&gt;를 플레이 해보자! 이제 막 여정에 올랐어. 나와 함께하자! 내 추천인 코드“Yaho12”를 입력하면 매우 좋은 선물을 받을 수 있어! http://t.co/yvTmOut1SP","oriTweetId":null,"date":"Thu Mar 28 12:47:10 +0000 2013","user":1128677209}
				String urlPattern = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
				String rtPattern = "(@-?[0-9]{6,14})\\s?";
				String repeatPattern = "[ㄱ-ㅎㅏ-ㅣ]+";
				String speicalPattern = "[~`!@#$%^&*()\\-_={}\\[\\]\\+\\|,.:\\?\"><\"]";
				String quotePattern = "\\s'";
				String ltrtPattern = "(lt;|gt;)";
				String langPattern = "([^'\\w가-힣]+)";
				String spacePattern = "\\s{2,}";

				msgStr = msgStr.replaceAll("\n"," ");	
				msgStr = msgStr.replaceAll(urlPattern, "");
				msgStr = msgStr.replaceAll(rtPattern, "");
				msgStr = msgStr.replaceAll(speicalPattern, " ");
				msgStr = msgStr.replaceAll(ltrtPattern, " ");
				msgStr = msgStr.replaceAll(langPattern, " ");
				msgStr = msgStr.replaceAll(repeatPattern, " ");
				msgStr = msgStr.replaceAll(quotePattern, " ");
				msgStr = msgStr.replaceAll(spacePattern, " ");
				msgStr = msgStr.trim();

				Calendar cal = Calendar.getInstance();
				Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH).parse(dateStr);
				cal.setTime(date);
				dateKey = new DateKey(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

				dateWordPair.setDate(dateKey);		
				//if(oriTweetId.isNull()) reporter.incrCounter(TwitRankerCounters.total_retweets, 1);
				//reporter.incrCounter("TweetsByDate", datekey.toString(), 1);
				
				//dateTxt.set(dateStr);
				//msgTxt.set(msgStr);
				//context.write(dateTxt, msgTxt);
				
				itr = new StringTokenizer(msgStr);
				while (itr.hasMoreTokens()) {
					wordTxt.set(itr.nextToken());
					dateWordPair.setWord(wordTxt);
					//dateWordPair.set(dateKey.toString() + "," + wordTxt);

					//System.out.println("wordTxt = " + wordTxt);
					//output.collect(dateWordPair, one);
					context.write(dateWordPair, one);
				}								
			}catch(Exception e){
				System.out.println(e.toString());
			}
		}
	}
	

	// REDUCER
	public static class TwitReducer extends Reducer<DateWordPair, IntWritable, Text, IntWritable> {

		private MultipleOutputs<Text, IntWritable> multipleOutputs;
		private IntWritable outputSum = new IntWritable();
		//private Text outputTxt = new Text();

		@Override
		public void setup(Context context) throws IOException, InterruptedException {
			multipleOutputs = new MultipleOutputs<Text, IntWritable>(context);
		}

		@Override
	    public void reduce(DateWordPair key, Iterable<IntWritable> values, Context output)
			throws IOException, InterruptedException {
			
			System.out.println("reducer = " + key.getSecond());
			int sum = 0;
			for(IntWritable value : values){
				sum += value.get();
			}			
			outputSum.set(sum);
			
			//output.write(key.getSecond(), outputSum);
			if(sum > 5)
				multipleOutputs.write("twitByDate", key.getSecond(), outputSum, (key.getFirst()).toString());
		}

		@Override
        public void cleanup(Context context) throws IOException, InterruptedException {
			multipleOutputs.close();
		}
	}
}
