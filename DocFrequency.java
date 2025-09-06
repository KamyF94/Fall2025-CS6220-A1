import java.io.IOException;
import java.util.HashSet;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class DocFrequency {

    public static class Map extends Mapper<Object, Text, Text, Text> {
        private Text word = new Text();
        private Text filename = new Text();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            String fileNameStr = ((FileSplit) context.getInputSplit()).getPath().getName();
            filename.set(fileNameStr);

            String[] tokens = value.toString().split("\\W+");
            for (String token : tokens) {
                if (token.length() > 0) {
                    word.set(token.toLowerCase());
                    context.write(word, filename);
                }
            }
        }
    }

    public static class Reduce extends Reducer<Text, Text, Text, IntWritable> {
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            HashSet<String> uniqueFiles = new HashSet<>();
            for (Text val : values) {
                uniqueFiles.add(val.toString());
            }
            context.write(key, new IntWritable(uniqueFiles.size()));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "doc frequency");
        job.setJarByClass(DocFrequency.class);
        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        // Mapper output
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        // Reducer output
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
