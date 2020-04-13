package io.elasticjob.lite.console.prometheus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.elasticjob.lite.lifecycle.api.JobAPIFactory;
import io.elasticjob.lite.lifecycle.api.JobStatisticsAPI;
import io.elasticjob.lite.lifecycle.api.ShardingStatisticsAPI;
import io.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import io.elasticjob.lite.lifecycle.domain.ShardingInfo;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Exporter extends Collector {

    private JobStatisticsAPI jobApi = JobAPIFactory
            .createJobStatisticsAPI(
                    ExporterProperties.getProperty("zookeeper.connect"),
                    ExporterProperties.getProperty("namespace"),
                    Optional.fromNullable(ExporterProperties
                            .getProperty("digest")));

    private ShardingStatisticsAPI shardApi = JobAPIFactory
            .createShardingStatisticsAPI(
                    ExporterProperties.getProperty("zookeeper.connect"),
                    ExporterProperties.getProperty("namespace"),
                    Optional.fromNullable(ExporterProperties
                            .getProperty("digest")));
    
    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> samples = new ArrayList<MetricFamilySamples>();
        // With no labels.
        //samples.add(new GaugeMetricFamily("my_gauge", "help", 42));
        GaugeMetricFamily totalGauge = new GaugeMetricFamily(
                "total_count_elastic_jobs",
                "total count of elastic jobs",
                jobApi.getJobsTotalCount());
        samples.add(totalGauge);
        
        // With labels
        GaugeMetricFamily jobGauge = new GaugeMetricFamily(
                "elastic_job",
                "elastic job running info, DISABLED 0, RUNNING 1, SHARDING_FLAG 2, PENDING 3",
                Arrays.asList("name", "server_ip", "failover", "item"));
        
        Iterator<JobBriefInfo> jobs = jobApi
                .getAllJobsBriefInfo().iterator();
        GaugeMetricFamily jobsGauge = new GaugeMetricFamily(
                "all_elastic_jobs",
                "all elastic jobs, OK 0, CRASHED 1, DISABLED 2, SHARDING_FLAG 3",
                Arrays.asList("name", "shard", "status"));
        for (; jobs.hasNext();) {
            JobBriefInfo job = jobs.next();
            jobsGauge.addMetric(Arrays.asList(
                    job.getJobName(),
                    "" + job.getShardingTotalCount(),
                    job.getStatus().name()),
                    job.getStatus().ordinal());
            
            Iterator<ShardingInfo> it = shardApi
                    .getShardingInfo(job.getJobName()).iterator();
            for (; it.hasNext();) {
                ShardingInfo info = it.next();
                jobGauge.addMetric(Arrays.asList(
                        job.getJobName(),
                        info.getServerIp(),
                        "" + info.isFailover(),
                        "" + info.getItem()),
                        info.getStatus().ordinal());
            }
        }
        samples.add(jobsGauge);
        samples.add(jobGauge);
        return samples;
    }
}
