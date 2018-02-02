import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
//import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
//import org.apache.hadoop.util.GenericOptionsParser;

public class TopN {

  public static void insert(PriorityQueue<ItemFreq> queue, String item, Long lValue, int topN) {
    ItemFreq head = (ItemFreq)queue.peek();

        // 큐의 원소수가 topN보다 작거나 지금 들어온 빈도수가 큐내의 최소 빈도수보다 크면
    if (queue.size() < topN || head.getFreq() < lValue) {
      ItemFreq itemFreq = new ItemFreq(item, lValue);
        // 일단 큐에 추가하고 
      queue.add(itemFreq);
        // 큐의 원소수가 topN보다 크면 가장 작은 원소를 제거합니다.
      // if (queue.size() > topN && head != null && head.getFreq() < lValue) {
      if (queue.size() > topN) {
          queue.remove();
      }
    }
  }
      
  public static class ItemFreqComparator implements Comparator<ItemFreq> {
	  	
	  //private int order;
	    
	  //public ItemFreqComparator(int order) {
		  // TODO Auto-generated constructor stub
		//   this.order = order;
	   // }
	   
    //@Override
    public int compare(ItemFreq x, ItemFreq y) {

      if (x.getFreq() < y.getFreq()) {
        return -1;
    	  // return this.order == 0 ? -1 : 1;
      		}
      if (x.getFreq() > y.getFreq()) {
    	  return 1;
    	  // return this.order == 0 ? 1 : -1;
      		}
      return 0;
    	}

  }

  public static class Map extends Mapper<Text, Text, Text, LongWritable> {

    //private final static LongWritable one = new LongWritable(1);
    
	  Comparator<ItemFreq> comparator = null;
	  //Comparator<ItemFreq> comparator = new ItemFreqComparator();
    
	  PriorityQueue<ItemFreq> queue = new PriorityQueue<ItemFreq>(10, comparator);
    
	  int topN = 10;
	  int order = 0;  // ascending(0)-(Oreumcha), descending(1)-(Naerimcha)

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
    	this.topN = context.getConfiguration().getInt("topN", 10);
    	this.order = context.getConfiguration().getInt("order", 0);
    	 
      //this.comparator = new ItemFreqComparator(this.order);
    	comparator = new ItemFreqComparator();
      this.queue = new PriorityQueue<ItemFreq>(10, comparator);
    	}

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {

    	 if (order == 0 ) {
		       while (queue.size() != 0) {
		         ItemFreq item = (ItemFreq)queue.remove();
		         context.write(new Text(item.getItem()), new LongWritable(item.getFreq()));
			         }
	       
	  	 } else {
	  		     Stack<ItemFreq> stack = new Stack<ItemFreq>();
	  		     while (queue.size() != 0) {
	  		         ItemFreq item = (ItemFreq)queue.remove();
	  		         //context.write(new Text(item.getItem()), new LongWritable(item.getFreq()));
	  		         stack.push(item);
	  			         }
	  		      
	  		     while (stack.size() != 0) {
	  		         ItemFreq item = stack.pop();
	  		         context.write(new Text(item.getItem()), new LongWritable(item.getFreq()));
	  			         }
	  		     
	  	      }

    	}

    public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        Long lValue = (long)Integer.parseInt(value.toString());

        insert(queue, key.toString(), lValue, topN);
        }
  } 
        
  public static class Reduce extends Reducer<Text, LongWritable, Text, LongWritable> {

	  
    Comparator<ItemFreq> comparator = null;
    
    PriorityQueue<ItemFreq> queue = new PriorityQueue<ItemFreq>(10, comparator);
    
    int topN = 10;
    int order = 0;
    
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
       this.topN = context.getConfiguration().getInt("topN", 10);
       this.order = context.getConfiguration().getInt("order", 0);
       comparator = new ItemFreqComparator();
       this.queue = new PriorityQueue<ItemFreq>(10, comparator);
        }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      
    	 if (order == 0 ) {
		       while (queue.size() != 0) {
		         ItemFreq item = (ItemFreq)queue.remove();
		         context.write(new Text(item.getItem()), new LongWritable(item.getFreq()));
			         }
	       
    	 } else {
    		     Stack<ItemFreq> stack = new Stack<ItemFreq>();
    		     while (queue.size() != 0) {
    		         ItemFreq item = (ItemFreq)queue.remove();
    		         //context.write(new Text(item.getItem()), new LongWritable(item.getFreq()));
    		         stack.push(item);
    			         }
    		      
    		     while (stack.size() != 0) {
    		         ItemFreq item = stack.pop();
    		         context.write(new Text(item.getItem()), new LongWritable(item.getFreq()));
    			         }
    		     
    	      }
      
	
	    }

    public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
        long sum = 0;
        for (LongWritable val : values) {
            sum += val.get();
                }

        insert(queue, key.toString(), sum, topN);
         }
 }
        
 public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    //String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    
    @SuppressWarnings("deprecation")
		Job job = new Job(conf, "TopN");

    job.setJarByClass(TopN.class);    
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(LongWritable.class);
        
    job.setMapperClass(Map.class);
    job.setReducerClass(Reduce.class);
    job.setNumReduceTasks(1);

    job.setInputFormatClass(KeyValueTextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    
    	// 파일 시스템 제어 객체 생성
 		FileSystem hdfs = FileSystem.get(conf);
 		// 경로 체크
 				
 		Path path = new Path(args[1]);
 		if (hdfs.exists(path)) {
 			hdfs.delete(path, true);
 		}
        
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    
    job.getConfiguration().setInt("topN", Integer.parseInt(args[2]));
    job.getConfiguration().setInt("order", Integer.parseInt(args[3]));
        
    job.waitForCompletion(true);
 }
        
}