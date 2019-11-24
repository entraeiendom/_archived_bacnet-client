package no.entra.bacnet.client;

import org.code_house.bacnet4j.wrapper.api.Device;
import org.code_house.bacnet4j.wrapper.ip.BacNetIpClient;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class BacnetClientTest {

    private BacNetIpClient client;
    private Device device;
    @Before
    public void setUp() throws Exception {
        client = mock(BacNetIpClient.class);
        device = mock(Device.class);
    }

    @Test
    public void discoverDeviceProperties() {
        assertTrue(true);
    }

}