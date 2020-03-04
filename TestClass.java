import org.junit.Ignore;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestClass {

    private LinkedList<String> list;

   /* @BeforeClass
    public static void m1() {
        System.out.println("Using @BeforeClass , executed before all test cases ");
    }

    @Before
    public void m2() {
        list = new LinkedList<String>();
        System.out.println("Using @Before annotations ,executed before each test cases ");
    }

    @AfterClass
    public static void m3() {
        System.out.println("Using @AfterClass ,executed after all test cases");
    }*/

    /*@After
    public void m4() {
        list.clear();
        System.out.println("Using @After ,executed after each test cases");
    }*/

    @Test
    public void m5() {
        list.add("test");
        System.out.println("Using @Test ,executed after each test cases");
    }

   /* @Ignore
    public void m6() {
        System.out.println("Using @Ignore , this execution is ignored");
    }

    @Property
    public void*/
}
