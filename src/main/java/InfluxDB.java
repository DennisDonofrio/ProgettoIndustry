import java.time.Instant;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

public class InfluxDB {
    private String token;

    private String bucket;

    private String org;

    private InfluxDBClient client;

    public InfluxDB(String token, String bucket, String org){
        this.token = token;
        this.bucket = bucket;
        this.org = org;

        client = InfluxDBClientFactory.create("http://192.168.50.10:8086", token.toCharArray());
    }

    public void write(MachineState machineState){
        Point point = Point
                .measurement("mem")
                .addTag("host", "host2")
                .addField("used_percent", Math.random())
                .time(Instant.now(), WritePrecision.NS);

        WriteApiBlocking writeApi = client.getWriteApiBlocking();
        writeApi.writePoint(bucket, org, point);
    }
}

