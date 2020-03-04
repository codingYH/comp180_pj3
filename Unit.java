import javax.management.ObjectName;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class Unit {
    public static HashMap<String, Throwable> testClass(String name) {
        Map<String, Method> mthMap = new HashMap<>();
        List<String> testMth = new LinkedList<String>();
        List<String> befClassMth = new LinkedList<String>();
        List<String> befMth = new LinkedList<String>();
        List<String> aftClassMth = new LinkedList<String>();
        List<String> aftMth = new LinkedList<String>();
        Class c = null;
        try {
            c = Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new NoSuchElementException("ClassNotFoundException: " + e);
        }
        Method[] methods = c.getMethods();
        for (Method m : methods) {
            mthMap.put(m.getName(),m);
            int count = 0;
            Annotation[] annotations = m.getAnnotations();
            for (Annotation a : annotations) {
               if (a.annotationType().equals(Test.class) ) {
                    if (count == 0) {
                        testMth.add(m.getName());
                        count++;
                    } else throw new NoSuchElementException("duplicated annotations " + m.getName());
                }
                if (a instanceof BeforeClass) {
                    if (count == 0) {
                        if (Modifier.isStatic(m.getModifiers())) {
                            befClassMth.add(m.getName());
                            count++;
                        } else throw new NoSuchElementException("BeforeClass annotations: not public static " +m.getName());
                    } else throw new NoSuchElementException("duplicated annotations " + m.getName());
                }
//                if (a .annotationType().equals(Before.class) ) {
                if (a instanceof Before ) {
                    if (count == 0) {
                        befMth.add(m.getName());
                        count++;
                    } else throw new NoSuchElementException("duplicated annotations " + m.getName());
                }
                if (a instanceof AfterClass) {
                    if (count == 0) {
                        if (Modifier.isStatic(m.getModifiers())) {
                            aftClassMth.add(m.getName());
                            count++;
                        } else throw new NoSuchElementException("AfterClass annotations: not public static " + m.getName());
                    } else throw new NoSuchElementException("duplicated annotations " + m.getName());
                }
                if (a instanceof After) {
                    if (count == 0) {
                        aftMth.add(m.getName());
                        count++;
                    } else throw new NoSuchElementException("duplicated annotations " +m.getName());
                }
            }
        }
        //sort all the methods name by alpha
        Collections.sort(testMth);
        Collections.sort(befClassMth);
        Collections.sort(befMth);
        Collections.sort(aftClassMth);
        Collections.sort(aftMth);
        HashMap<String, Throwable> resl = new HashMap<String, Throwable>();
        //call all beforeClass methods
        invokeMths(mthMap, c, befClassMth);

        //call all Test methods
        for (int i = 0; i < testMth.size(); i++) {
            Object instance = null;
            try {
                //instance should call bef-test-after, can not initialize every method
                instance = c.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            //assume no exception
            resl.put(testMth.get(i), null);
            //all before methods
            invokeMths(mthMap, instance, befMth);
            //i test case
            try {
                Method tm = mthMap.get(testMth.get(i));
                tm.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                //exception
                resl.put(testMth.get(i), e);
            }
            //all after methods
            invokeMths(mthMap, instance, aftMth);
        }
        //all afterClass methods
        invokeMths(mthMap, c, aftClassMth);
        //return result map
        return resl;
    }

    //invoke all instance method
    private static void invokeMths(Map<String, Method> mthMap, Object instance, List<String> mths){
        for (int i = 0; i < mths.size(); i++) {
            try {
                Method m = mthMap.get(mths.get(i));
                //static,
                    m.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new NoSuchElementException(" method invoke wrong: " + e);
            }
        }
    }

    //invoke all static method
    private static void invokeMths(Map<String, Method> mthMap, Class c, List<String> mths){
        for (int i = 0; i < mths.size(); i++) {
            try {
                Method m = mthMap.get(mths.get(i));
                //static,
                m.invoke(c);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new NoSuchElementException(" method invoke wrong: " + e);
            }
        }
    }

    public static HashMap<String, Object[]> quickCheckClass(String name) {
        Map<String, Method> mthMap = new HashMap<>();
        List<String> proptMth = new LinkedList<String>();
        HashMap<String, Object[]> resl = new HashMap<String, Object[]>();
        Class c = null;
        try {
            c = Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new NoSuchElementException("ClassNotFoundException: " + e);
        }
        Method[] methods = c.getMethods();
        for (Method m : methods) {
            mthMap.put(m.getName(), m);
            Annotation propAn = m.getAnnotation(Property.class);
            //has Property annotation
            if ((propAn != null) && propAn instanceof Property) {
                proptMth.add(m.getName());
            }
        }
        //sort by alpha
        Collections.sort(proptMth);
        // invoke propMth
        for (int i = 0; i < proptMth.size(); i++) {
            //assume no exception
            resl.put(proptMth.get(i), null);
            Method propM = mthMap.get(proptMth.get(i));
            Class[] paraType = propM.getParameterTypes();
            Annotation[][] paraAnn = propM.getParameterAnnotations();
            Object[] paras = null;
            //repeat 100 times
            for (int rep = 0; rep < 100; rep ++){
                Object instance = null;
                try {
                    instance = c.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                paras = getArgs(mthMap, c, paraType, paraAnn, instance);
                try {
                    propM.invoke(instance, paras);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    //exception
                    resl.put(proptMth.get(i), paras);
                    break;
                }
            }
        }
        return resl;
    }

    private static Object[] getArgs(Map<String, Method> mthMap, Class testClass, Class[] paraType, Annotation[][] paraAnn, Object instance){
        //args array
        Object[] paras = new Object[paraType.length];
        //generate args
        for (int i = 0; i< paraType.length; i++){
            if(paraType[i].equals(Integer.class)||paraType[i].equals(String.class)
                    ||paraType[i].equals(List.class)||paraType[i].equals(Object.class)){
                // return one of paraType[i] argument
                paras[i] = getArgByAnn(mthMap, testClass, paraAnn[i], instance);
            }else throw new NoSuchElementException("Argument type wrong");
        }
        return paras;
    }

    private static Object getArgByAnn(Map<String, Method> mthMap, Class testClass, Annotation[] ann, Object instance){
        //get int
        if(ann[0] instanceof IntRange){
            IntRange intRange = (IntRange)ann[0];
           return getRandomIntInRange(intRange.min(), intRange.max());
        }
        //get string
        if(ann[0] instanceof StringSet){
            StringSet strSet = (StringSet)ann[0];
            String[] s = strSet.strings();
            return s[getRandomIntInRange(0, s.length - 1)];
        }
        //get list
        if(ann[0] instanceof ListLength){
            ListLength lAnn = (ListLength) ann[0];
            //generate list's length
            int len = getRandomIntInRange(lAnn.min(), lAnn.max());
            List l = new LinkedList();
            //generate list of len length
            for (int i = 0; i < len; i++){
                Annotation[] eleAnn = Arrays.copyOfRange(ann, 1, ann.length);
                //recursive call getArgByAnn
                l.add(getArgByAnn(mthMap, testClass, eleAnn, instance));
            }
            return l;
        }
        //get object
        if(ann[0] instanceof ForAll){
            ForAll forAll = (ForAll) ann[0];
            String mthName = forAll.name();
            int times = forAll.times();
            // pick a random time
            int randCallTimes = getRandomIntInRange(1, times);
            Object re = null;
            try {
                Method mth = mthMap.get(mthName);
                for (int t = 0; t < randCallTimes - 1; t++) {
                    mth.invoke(instance);
                }
                //call randCallTimes time
                re = mth.invoke(instance);
            }catch ( IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    throw new NoSuchElementException("ForAll: " + e);
                }
            return re;
        }
        //invalid para annotation
        else throw new NoSuchElementException("Argument Annotation wrong");
    }

    //a int range from min to max, inclusive
    private static Integer getRandomIntInRange(int min, int max){
        Random r = new Random();
        return min + r.nextInt(max - min + 1);
    }













}