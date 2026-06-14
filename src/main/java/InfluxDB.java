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

    private WriteApiBlocking writeApi;

    public InfluxDB(String token, String bucket, String org){
        this.token = token;
        this.bucket = bucket;
        this.org = org;

        client = InfluxDBClientFactory.create("http://192.168.50.10:8086", token.toCharArray());
        writeApi = client.getWriteApiBlocking();
    }

    public void write(MachineState machineState){
        //+System.out.println("Write");
        Point point = Point
                .measurement("line_state")
                .addTag("host", "host1")

                .addField("leftGateCounterValue", machineState.getLeftGateCounter())
                .addField("rightGateCounterValue", machineState.getRightGateCounter())
                .addField("leftDepoCounterValue", machineState.getLeftDepoCounter())
                .addField("rightDepoCounterValue", machineState.getRightDepoCounter())

                .addField("leftGateOpenValue", machineState.isLeftGateOpen() ? 1 : 0)
                .addField("rightGateOpenValue", machineState.isRightGateOpen() ? 1 : 0)
                .addField("sortingOpenValue", machineState.isSortingOpen() ? 1 : 0)

                .addField("leftBatchPresentValue", machineState.isLeftBatchPresent() ? 1 : 0)
                .addField("rightBatchPresentValue", machineState.isRightBatchPresent() ? 1 : 0)

                .time(System.currentTimeMillis(), WritePrecision.MS);
        //System.out.println(point.toLineProtocol());

        writeApi.writePoint(bucket, org, point);
    }
}

