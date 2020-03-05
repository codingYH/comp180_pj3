import org.junit.Assert;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class TestClass {

    private LinkedList<String> list;

    @BeforeClass
    public static void m1() {
//        System.out.println("Using @BeforeClass , executed before all test cases ");
    }

    @Before
    public void m2() {
        list = new LinkedList<String>();
//        System.out.println("Using @Before annotations ,executed before each test cases ");
    }

    @AfterClass
    public static void m3() {
//        System.out.println("Using @AfterClass ,executed after all test cases");
    }

    @After
    public void m4() {
        list.clear();
//        System.out.println("Using @After ,executed after each test cases");
    }

    @Test
    public void m5() {
        list.add("test");
//        System.out.println("Using @Test ,executed after each test cases");
    }

    @Ignore
    public void m6() {
//        System.out.println("Using @Ignore , this execution is ignored");
    }

    @Property
    public void testInt(@IntRange(min = -3, max = 500) Integer i){
        System.out.println("testInt: " + i);
        Assert.assertTrue(i < 70);
    }

    @Property
    public void testStringSet(@StringSet(strings = {"a","as", "baq", "aw","eas", "bfa", "ae","ase", "bea", "ea","aes", "baw"}) String i){
        System.out.println("testStringSet: " + i);
    }


    @Property
    public void testList(@ListLength(min = 0, max = 0) List<@IntRange(min = 1, max = 30) Integer>  l, @IntRange(min = 1, max = 107) Integer i){
        System.out.println("testList: " + l + " " + i);
    }

    @Property
    public void testList2(@ListLength(min = 0, max = 3) List<@IntRange(min = 1, max = 2) Integer>  l){
        System.out.println("testList2: " + l + " ");
    }


    @Property
    public void testObject(@ForAll(name="genIntSet", times=10) Object o) {
        HashSet s = (HashSet) o;
        s.add("foo");
        System.out.println("testObject: s.contains(\"foo\"): " + s.contains("foo")); ;
    }

    int count = 0;
    public Object genIntSet() {
        HashSet s = new HashSet();
        for (int i=0; i<count; i++) { s.add(i); }
        count++;
        return s;
    }
}
