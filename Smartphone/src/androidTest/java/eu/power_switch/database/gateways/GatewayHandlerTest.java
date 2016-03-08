//package eu.power_switch.database.gateways;
//
//import android.test.IsolatedContext;
//import android.test.suitebuilder.annotation.SmallTest;
//import eu.power_switch.log.Log;
//import android.support.test.runnner.AndroidJUnit4;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import eu.power_switch.database.gateways.GatewayHandler;
//import eu.power_switch.obj.gateways.ConnAir;
//
//@RunWith(AndroidJUnit4.class)
//@SmallTest
//public class GatewayHandlerTest {
//
//    IsolatedContext context;
////    public GatewayHandlerTest() {
////
////    }
//    @Test
//    public void setUp() throws Exception {
//        context = new IsolatedContext(null, null);
//        assertNotNull(context);
//    }
//
//    @Test
//    public void testAdd() throws Exception {
////        ConnAir gw = Mockito.mock(ConnAir.class);
////        Mockito.when(gw.isActive()).thenReturn(true);
////        Mockito.when(gw.getName()).thenReturn("dummy");
////        Mockito.when(gw.getModelAsString()).thenReturn(ConnAir.MODEL);
////        Mockito.when(gw.getFirmware()).thenReturn("firmware");
////        Mockito.when(gw.getHost()).thenReturn("10.10.10.10");
////        Mockito.when(gw.getPort()).thenReturn(1000);
////
//        ConnAir gw = new ConnAir(0, true, "dummy", "firmware", "10.10.10.10", 1000);
//        GatewayHandler handler = new GatewayHandler(context);
//        long id = handler.add(gw);
//        Log.d("GatewayHandlerTest", "Gateway ID:" + id);
////        Mockito.verify(gw).isActive();
////        Mockito.verify(gw).getName();
////        Mockito.verify(gw).getModelAsString();
////        Mockito.verify(gw).getFirmware();
////        Mockito.verify(gw).getHost();
////        Mockito.verify(gw).getPort();
////        Mockito.verify(gw).getId();
////        assertEquals(0, -1);
//        assertTrue(false);
//    }
//
////    public void testGet() throws Exception {
//////        GatewayHandler handler = Mockito.mock(GatewayHandler.class);
////
////    }
//}