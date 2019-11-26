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
    private static boolean findAllPropertyValues = true;
    private static boolean discoverDevcices = true;
    private static boolean findDeviceProprperties = true;

    public static void main(String[] args) {
        int clientDeviceId = 2002;
        IpNetwork ipNetwork = null;
        String ip = "192.168.1.250";
        ip = "0.0.0.0";
        String broadcast = "192.168.1.255";
        broadcast = "255.255.255.255";
        int port = 47808;
        ipNetwork = new IpNetworkBuilder()
                .withLocalBindAddress(ip)
                .withReuseAddress(true)
                .withBroadcast(broadcast, 24)
                .withPort(port)
                .build();
        BacNetIpClient client = new BacNetIpClient(ipNetwork, clientDeviceId);
        client.start();

        Set<Device> devicesFound = null;
        if (discoverDevcices) {
            devicesFound = discoverDeviceProperties(client);
        }

        if (findDeviceProprperties && devicesFound != null) {
            scanPropertiesForAllDevices(client, devicesFound);
        }
        log.info("Done.");
        client.stop();
    }

    public static void scanPropertiesForAllDevices(BacNetIpClient client, Set<Device> devices) {
        Random random = new Random();
        for (Device device : devices) {
            try {
                Thread.sleep(random.nextInt(500));
            } catch (InterruptedException e) {
                log.trace("Interupted");
            }
            log.info("Discover Device: {}", device);
            List<Property> availableProperties = discoverDeviceProperties(client, device);
            if(findAllPropertyValues && availableProperties != null) {
                findPropertyValues(client,device,availableProperties);
            }
        }
    }

    public static Set<Device> discoverDeviceProperties(BacNetIpClient client) {
        log.info("Discovering devices.");

        Set<Device> devices = client.discoverDevices(5000); // given number is timeout in millis
        log.info("Found devices: " + devices.size());
        //serialize(devices, "devices.ser");

        return devices;
    }

    public static List<Property> discoverDeviceProperties(BacNetIpClient client, Device device) {
        List<Property> deviceProperties = null;
        try {
            deviceProperties = client.getDeviceProperties(device);

            if (deviceProperties != null) {
                log.info("Device name: {} has {} properties.", device.getName(), deviceProperties.size());
            } else {
                log.debug("No device properties found for device {}", device);
            }
        } catch (Exception e) {
            log.info("Failed to find info for device {}", device, e);
        }
        return deviceProperties;
    }

    public static void findPropertyValues(BacNetIpClient client, Device device, List<Property> deviceProperties) {
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
    }

    public static class StringBacNetToJavaConverter implements BacNetToJavaConverter<String> {
        @Override
        public String fromBacNet(com.serotonin.bacnet4j.type.Encodable encodable) {
            return encodable.toString();
        }
    }
}
