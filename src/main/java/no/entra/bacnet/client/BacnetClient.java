package no.entra.bacnet.client;

import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import org.code_house.bacnet4j.wrapper.api.BacNetClientException;
import org.code_house.bacnet4j.wrapper.api.BacNetToJavaConverter;
import org.code_house.bacnet4j.wrapper.api.Device;
import org.code_house.bacnet4j.wrapper.api.Property;
import org.code_house.bacnet4j.wrapper.ip.BacNetIpClient;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class BacnetClient {
    private static final Logger log = getLogger(BacnetClient.class);

    public static void main(String[] args) {
        int clientDeviceId = 2002;
        boolean discoverDevcices = true;
        IpNetwork ipNetwork = null;
        String ip = "192.168.1.31";
        String broadcast = "192.168.1.255";
        int port = 47808;
        ipNetwork = new IpNetworkBuilder()
                .withLocalBindAddress(ip)
                .withReuseAddress(true)
                .withBroadcast(broadcast, 24)
                .withPort(port)
                .build();
        BacNetIpClient client = new BacNetIpClient(ipNetwork, clientDeviceId);
        client.start();

        if (discoverDevcices) {
            Set<Device> devices = discoverDeviceProperties(client);

            Random random = new Random();
            for (Device device : devices) {
                try {
                    Thread.sleep(random.nextInt(500));
                } catch (InterruptedException e) {
                    log.trace("Interupded");
                }
                log.info("Discover Device: {}", device);
                discoverDeviceProperties(client, device);
            }
        }
        try {
            int sec = 20;
            log.info("Waiting {} seconds for bacnet messages to arrive.", sec);
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            log.debug("Interupted. That's ok");
        }
        log.info("Done.");
        client.stop();
    }

    public static Set<Device> discoverDeviceProperties(BacNetIpClient client) {
        log.info("Discovering devices.");

        Set<Device> devices = client.discoverDevices(5000); // given number is timeout in millis
        log.info("Found devices: " + devices.size());
        //serialize(devices, "devices.ser");

       return devices;
    }

    public static void discoverDeviceProperties(BacNetIpClient client, Device device) {
        try {
            List<Property> deviceProperties = client.getDeviceProperties(device);

            if (deviceProperties != null) {
                log.info("Device name: {} has {} properties.", device.getName(), deviceProperties.size());
                for (Property property : deviceProperties) {
                    log.info("Device: {}, Property {}. Looking for value.", device.getName(), property);
                    BacNetToJavaConverter<String> converter = new StringBacNetToJavaConverter();
                    try {
                    String presentValue = client.getPropertyValue(property, converter);
                    log.info("Device name: {}; Property name: {}; value {} ", device.getName(), property.getName(), presentValue);
                    } catch (BacNetClientException e) {
                        log.debug("Property could not be read. Device name: {} Property name: {}, Property: {}. ", device.getName(), property.getName(), property);
                    }
                }
            } else {
                log.debug("No device properties found for device {}", device);
            }
        } catch (Exception e) {
            log.info("Failed to find info for device {}", device, e);
        }
    }

    public static class StringBacNetToJavaConverter implements BacNetToJavaConverter<String> {
        @Override
        public String fromBacNet(com.serotonin.bacnet4j.type.Encodable encodable) {
            return encodable.toString();
        }
    }
}
