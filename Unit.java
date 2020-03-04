import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class Unit {
    public static HashMap<String, Throwable> testClass(String name) {
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
        int count = 0;
        for (Method m : methods) {
            Annotation[] annotations = m.getAnnotations();
            for (Annotation a : annotations) {
               if (a instanceof Test) {
                    if (count == 0) {
                        testMth.add(m.getName());
                        count++;
                    } else throw new NoSuchElementException("duplicated annotations");
                }
                if (a instanceof BeforeClass) {
                    if (count == 0) {
                        if (Modifier.isStatic(m.getModifiers())) {
                            befClassMth.add(m.getName());
                            count++;
                        } else throw new NoSuchElementException("BeforeClass annotations: not public static");
                    } else throw new NoSuchElementException("duplicated annotations");
                }
                if (a instanceof Before) {
                    if (count == 0) {
                        befMth.add(m.getName());
                        count++;
                    } else throw new NoSuchElementException("duplicated annotations");
                }
                if (a instanceof AfterClass) {
                    if (count == 0) {
                        if (Modifier.isStatic(m.getModifiers())) {
                            aftClassMth.add(m.getName());
                            count++;
                        } else throw new NoSuchElementException("AfterClass annotations: not public static");
                    } else throw new NoSuchElementException("duplicated annotations");
                }
                if (a instanceof After) {
                    if (count == 0) {
                        aftMth.add(m.getName());
                        count++;
                    } else throw new NoSuchElementException("duplicated annotations");
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
        invokeMths(c, befClassMth, true);

        //call all Test methods
        for (int i = 0; i < testMth.size(); i++) {
            //assume no exception
            resl.put(testMth.get(i), null);
            //all before methods
            invokeMths(c, befMth, false);
            //i test case
            try {
                Method tm = c.getMethod(testMth.get(i));
                tm.invoke(c.getDeclaredConstructor().newInstance());
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                //exception
                resl.put(testMth.get(i), e);
            }
            //all after methods
            invokeMths(c, aftMth, false);
        }
        //all afterClass methods
        invokeMths(c, aftClassMth, true);
        //return result map
        return resl;
    }

    //invoke all method in mths list
    public static void invokeMths(Class c, List<String> mths, boolean isStatic){
        for (int i = 0; i < mths.size(); i++) {
            try {
                Method m = c.getMethod(mths.get(i));
                //static,
                if (isStatic == true){
                    m.invoke(c);
                }else m.invoke(c.getDeclaredConstructor().newInstance());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
                throw new NoSuchElementException(" method invoke wrong: " + e);
            }
        }
    }

    public static HashMap<String, Object[]> quickCheckClass(String name) {
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
            try {
                Method propM = c.getMethod(proptMth.get(i));
                Class[] paraType = propM.getParameterTypes();
                Annotation[][] paraAnn = propM.getParameterAnnotations();
                Object[] paras = null;
                //repeat 100 times
                for (int rep = 0; rep < 100; rep ++){
                    paras = getArgs(c, paraType, paraAnn);
                    try {
                        propM.invoke(c.getDeclaredConstructor().newInstance(), paras);
                    } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                        e.printStackTrace();
                        //exception
                        resl.put(proptMth.get(i), paras);
                        break;
                    }
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                throw new NoSuchElementException("NoSuchMethod" + e);
            }
        }
        return resl;
    }

    public static Object[] getArgs(Class testClass, Class[] paraType, Annotation[][] paraAnn){
        //args array
        Object[] paras = new Object[paraType.length];
        //generate args
        for (int i = 0; i< paraType.length; i++){
            if(paraType[i].equals(Integer.class)||paraType[i].equals(String.class)
                    ||paraType[i].equals(List.class)||paraType[i].equals(Object.class)){
                // return one of paraType[i] argument
                paras[i] = getArgByAnn(testClass, paraAnn[i]);
            }else throw new NoSuchElementException("Argument type wrong");
        }
        return paras;
    }

    public static Object getArgByAnn(Class testClass, Annotation[] ann){
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
                l.add(getArgByAnn(testClass, eleAnn));
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
                Method mth = testClass.getMethod(mthName);
                for (int t = 0; t < randCallTimes - 1; t++) {
                    mth.invoke(testClass.getDeclaredConstructor().newInstance());
                }
                //call randCallTimes time
                re = mth.invoke(testClass.getDeclaredConstructor().newInstance());
            }catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                    throw new NoSuchElementException("ForAll: " + e);
                }
            return re;
        }
        //invalid para annotation
        else throw new NoSuchElementException("Argument Annotation wrong");
    }

    //a int range from min to max, inclusive
    public static Integer getRandomIntInRange(int min, int max){
        Random r = new Random();
        return min + r.nextInt(max - min + 1);
    }













}