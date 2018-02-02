package wordcount ;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;  
import org.apache.hadoop.io.IntWritable;  
import org.apache.hadoop.io.Text;  
import org.apache.hadoop.mapreduce.Job;  
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;  
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;  
import org.apache.hadoop.util.GenericOptionsParser; 

public class WordCount02 {  
	
	/*public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable>{  
		private final static IntWritable one = new IntWritable(1);  
		private Text word = new Text();  
		public void map(Object key, Text value, Context context 
				) throws IOException, InterruptedException {  
			StringTokenizer itr = new StringTokenizer(value.toString(), ",");  
			while (itr.hasMoreTokens()) {  
				word.set(itr.nextToken());  
				context.write(word, one);  
			}
		}
	}  */
	////////////////////////////////////////////////////////////////////////////////////
	/*public static class IntSumReducer 
		extends Reducer<Text,IntWritable,Text,IntWritable> {  
		private IntWritable result = new IntWritable();  

		public void reduce(Text key, Iterable<IntWritable> values, Context context 
				) throws IOException, InterruptedException {  
			int sum = 0;  
			for (IntWritable val : values) {  
				sum += val.get();  
			}  
			result.set(sum);  
			context.write(key, result);  
		}  
	}*/
	/////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) throws Exception {  
		
		Configuration conf = new Configuration();  
		
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();  
		
		if (otherArgs.length != 2) {  
			System.err.println("Usage: wordcount02 <in> <out>");  
			System.exit(2);  
			//System.exit(0); 
		}
		
		Job job = new Job(conf, "word count 02");  
		
		job.setJarByClass(WordCount02.class);  
		job.setMapperClass(TokenizerMapper.class);  
		job.setCombinerClass(IntSumReducer.class);  
		job.setReducerClass(IntSumReducer.class);  
		job.setOutputKeyClass(Text.class);  
		job.setOutputValueClass(IntWritable.class); 
		
		// output folder 
		FileSystem hdfs = FileSystem.get(conf);
		
		Path path = new Path(args[1]);
		if (hdfs.exists(path)) {
			hdfs.delete(path, true);
		}
		
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));  
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));  
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);   
	}  
}