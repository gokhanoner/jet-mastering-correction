import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.pipeline.BatchStage;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.query.Predicates;

import java.util.Map;

public class JetProc {

    public static void main(String[] args) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("127.0.0.1:5701");

        JetInstance jet = Jet.newJetInstance();

        Pipeline p = Pipeline.create();

        p
                .drawFrom(Sources.<Integer, Integer, Integer>remoteMap("test", clientConfig, Predicates.alwaysTrue(), Map.Entry::getValue))
                .drainTo(Sinks.logger());

        jet.newJob(p).join();
    }
}
