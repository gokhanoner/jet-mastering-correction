import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

public class Member {

    public static void main(String[] args) {
        System.setProperty("hazelcast.phone.home.enabled", "false");
        System.setProperty("hazelcast.enterprise.license.key","HazelcastEnterpriseHD#2Nodes#maOHFiwR5YEcy1T6K7bJ0u290q21h9d19g00sX99C39399eG99Z9v0x9t9x0");

        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        }
}
