package wordcount;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

	private final static IntWritable one = new IntWritable(1);  
	private Text word = new Text();
	
	@Override
	protected void map(Object key, Text value,   
										Mapper<Object, Text, Text, IntWritable>.Context context 
										) throws IOException, InterruptedException {  
		
//		StringTokenizer itr = new StringTokenizer(value.toString(), " ");  
		/*
		while (itr.hasMoreTokens()) {  
			word.set(itr.nextToken());  
			context.write(word, one);  
		}*/
		
		String[] citation = value.toString().split(" ");
		//context.write(new Text(citation[1]), one);
		
		for(String item : citation) {
			word.set(item);
		  context.write(word, one);  
		}
		
	}
}
