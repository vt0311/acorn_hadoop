package wordcount;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class IntSumReducer extends Reducer<Object, Text, Text, IntWritable> {
		
	    private IntWritable result = new IntWritable(1);
	
			public void reduce(Text key, Iterable<IntWritable> values, 
					Reducer<Object, Text, Text, IntWritable>.Context context 
					) throws IOException, InterruptedException {
				
					int sum = 0;  
					for (IntWritable val : values) {  
						sum += val.get();  
					}  
					result.set(sum);  
					context.write(key, result);  
			}
}