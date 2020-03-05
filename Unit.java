import javax.management.ObjectName;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class Unit {
    private final static int listParaOffSet = 100;

    public static HashMap<String, Throwable> testClass(String name) {
        Map mthType = getMthType(name);
        Map<String, Method> mthMap = new HashMap<>();
        List<String> testMth = (List<String>) mthType.get("testMth");
        List<String> befClassMth = (List<String>) mthType.get("befClassMth");
        List<String> befMth = (List<String>) mthType.get("befMth");
        List<String> aftClassMth = (List<String>) mthType.get("aftClassMth");
        List<String> aftMth = (List<String>) mthType.get("aftMth");
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
        }
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
                //exception, !put getCause rather than e itself
                resl.put(testMth.get(i), e.getCause());
            }
            //all after methods
            invokeMths(mthMap, instance, aftMth);
        }
        //all afterClass methods
        invokeMths(mthMap, c, aftClassMth);
        //return result map
        return resl;
    }
    public static HashMap<String, Object[]> quickCheckClass(String name) {
        Map mthType = getMthType(name);
        Map<String, Method> mthMap = new HashMap<>();
        List<String> proptMth = (List<String>) mthType.get("proptMth");
        List<String> befClassMth = (List<String>) mthType.get("befClassMth");
        List<String> befMth = (List<String>) mthType.get("befMth");
        List<String> aftClassMth = (List<String>) mthType.get("aftClassMth");
        List<String> aftMth = (List<String>) mthType.get("aftMth");
        HashMap<String, Object[]> resl = new HashMap<String, Object[]>();
        Class c = null;
        try {
            c = Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new NoSuchElementException("ClassNotFoundException: " + e);
        }
        //get instance
        Object instance = null;
        try {
            instance = c.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        Method[] methods = c.getMethods();
        for (Method m : methods) {
            mthMap.put(m.getName(), m);
        }
        //call all beforeClass methods
        invokeMths(mthMap, c, befClassMth);
        // invoke propMth
        for (int i = 0; i < proptMth.size(); i++) {
            //assume no exception
            resl.put( proptMth.get(i), null);
            List<List> paraArray = new LinkedList();
            Method propM = mthMap.get(proptMth.get(i));
            AnnotatedType[] paras = propM.getAnnotatedParameterTypes();
            //get all possible arg, put into paraMap
            for (int pi = 0; pi < paras.length; pi++) {
                Object[] paraList = new Object[paras.length];
                // @ListLength(min=0, max=2) List<T>
                if (paras[pi] instanceof AnnotatedParameterizedType) {
                    if (paras[pi].getAnnotations()[0] instanceof ListLength) {
                        ListLength listLength = (ListLength) paras[pi].getAnnotations()[0];
                        //get generic type
                        AnnotatedParameterizedType p = (AnnotatedParameterizedType) paras[pi];
                        AnnotatedType[] genericType = p.getAnnotatedActualTypeArguments();
                        Annotation genericAnn = genericType[0].getAnnotations()[0];
                        List lPara = getArgByAnn(genericAnn, mthMap, instance);
                        paraArray.add(getAllPossibleListPara(listLength.min(), listLength.max(), lPara));
                    }
                } else {
                    paraArray.add(getArgByAnn(paras[pi].getAnnotations()[0], mthMap, instance));
                }
            }
            // get all possible combination of para
            List<Object[]> paraL = getAllParaInstance(paraArray);
            //the first is 1
            int invokeCount = 1;
            for (Object[] p : paraL) {
                //at most invoke 100
                if (invokeCount > 100) {
                    break;
                } else {
                    //all before methods
                    invokeMths(mthMap, instance, befMth);
                    try {
                        invokeCount++;
                        propM.invoke(instance, p);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        resl.put(propM.getName(), p);
                        break;
                    }
                    //all after methods
                    invokeMths(mthMap, instance, aftMth);
                }
            }
        }
        //all afterClass methods
        invokeMths(mthMap, c, aftClassMth);
        return resl;
    }

   /* private static List<List>  prepArg(Map<String, Method> mthMap, List<String> proptMth){

    }*/


    private static List getArgByAnn(Annotation ann, Map<String, Method> mthMap, Object instance) {
        List paraL = new LinkedList();
        //get int
        if (ann instanceof IntRange) {
            IntRange intRange = (IntRange) ann;
            for (int i = intRange.min(); i <= intRange.max(); i++) {
                paraL.add(i);
            }
            return paraL;
        }
        //get string
        if (ann instanceof StringSet) {
            StringSet strSet = (StringSet) ann;
            for (String s : strSet.strings()) {
                paraL.add(s);
            }
            return paraL;
        }
        if (ann instanceof ForAll) {
            ForAll forAll = (ForAll) ann;
            String mthName = forAll.name();
            try {
                Method mth = mthMap.get(mthName);
                for (int t = 0; t < forAll.times(); t++) {
                    mth.invoke(instance);
                    paraL.add(mth.invoke(instance));
                }
                //call randCallTimes time

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new NoSuchElementException("ForAll: " + e);
            }
            return paraL;
        }
        if (ann instanceof ListLength) {
            ListLength listLength = (ListLength) ann;
            for (int i = listLength.min(); i <= listLength.max(); i++) {
                paraL.add(i);
            }
            return paraL;
        }
        //invalid para annotation
        else throw new NoSuchElementException("Argument Annotation wrong " + ann);
    }

    //return list of list
    private static List getAllPossibleListPara(int min, int max, List lPara) {
        List<List> ll = new LinkedList();
        for (int l = min; l <= max; l++) {
            if (ll.size() > 110) {
                return ll;
            } else if (getAllPossibleListParaByLength(l, lPara) == null){
                ll.add(new LinkedList());
                return ll;
            }
            else {
                ll.addAll(getAllPossibleListParaByLength(l, lPara));
            }
        }
        return ll;
    }

    //return list of list
    private static List getAllPossibleListParaByLength(int length, List lPara) {
        List ll = new LinkedList();
        if(length == 0){
            return null;
        }
         else if (ll.size() > 110){
            return ll;
        } else if (length == 1) {
            for (Object o : lPara) {
                LinkedList lOne = new LinkedList();
                lOne.add(o);
                ll.add(lOne);
            }
            return ll;
        } else {
            for (Object o : lPara) {
                //lMore is a list<Object> size = length - 1
                for (Object lMore : getAllPossibleListParaByLength(length - 1, lPara)) {
                    List lm = (List) lMore;
                    lm.add(o);
                    ll.add(lm);
                }
            }return ll;
        }
    }

    // @Para  paraArray List<List>
    // @return list<Object[]> each is possible para Array
    private static List<Object[]> getAllParaInstance(List<List> paraArray) {
        List<Object[]> paraInstanceArrayList = new LinkedList();
        // para size only 1
        if (paraArray.size() == 1) {
            //paraArray.get(0) is list of possible values for this 0-th para
            for (Object o : paraArray.get(0)) {
                Object[] paras = new Object[1];
                paras[0] = o;
                paraInstanceArrayList.add(paras);
            }
            return paraInstanceArrayList;
            //para more than 110
        } else if (paraInstanceArrayList.size() > 110) {
            return paraInstanceArrayList;
        } else {
            // //paraArray.get(paraArray.size() - 1) is
            // list of possible values for this paraArray.size() - 1-th para
            for (Object o : paraArray.get(paraArray.size() - 1)) {
                // oa[paraArray.size() - 1]
                for (Object[] oa : getAllParaInstance(paraArray.subList(0, paraArray.size() - 1))) {
                    //copy and resize oa to paras[paraArray.size()]
                    Object[] paras = Arrays.copyOf(oa, paraArray.size());
                    //add the paraArray.size() - 1 -th para
                    paras[paraArray.size() - 1] = o;
                    // add this paras
                    paraInstanceArrayList.add(paras);
                }
            }
            return paraInstanceArrayList;
        }
    }

    private static HashMap<String, List<String>> getMthType(String name) {
        HashMap<String, List<String>> rel = new HashMap<String, List<String>>();
        Map<String, Method> mthMap = new HashMap<>();
        List<String> testMth = new LinkedList<String>();
        List<String> befClassMth = new LinkedList<String>();
        List<String> befMth = new LinkedList<String>();
        List<String> aftClassMth = new LinkedList<String>();
        List<String> aftMth = new LinkedList<String>();
        List<String> proptMth = new LinkedList<String>();
        Class c = null;
        try {
            c = Class.forName(name);
        } catch (
                ClassNotFoundException e) {
            e.printStackTrace();
            throw new NoSuchElementException("ClassNotFoundException: " + e);
        }
        Method[] methods = c.getMethods();
        for (Method m : methods) {
            mthMap.put(m.getName(), m);
            int count = 0;
            Annotation[] annotations = m.getAnnotations();
            for (Annotation a : annotations) {
                if (a.annotationType().equals(Test.class)) {
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
                        } else
                            throw new NoSuchElementException("BeforeClass annotations: not public static " + m.getName());
                    } else throw new NoSuchElementException("duplicated annotations " + m.getName());
                }
//                if (a .annotationType().equals(Before.class) ) {
                if (a instanceof Before) {
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
                        } else
                            throw new NoSuchElementException("AfterClass annotations: not public static " + m.getName());
                    } else throw new NoSuchElementException("duplicated annotations " + m.getName());
                }
                if (a instanceof After) {
                    if (count == 0) {
                        aftMth.add(m.getName());
                        count++;
                    } else throw new NoSuchElementException("duplicated annotations " + m.getName());
                }
                if (a instanceof Property) {
                    if (count == 0) {
                        proptMth.add(m.getName());
                        count++;
                    } else throw new NoSuchElementException("duplicated annotations " + m.getName());
                }
            }
        }
        //sort all the methods name by alpha
        Collections.sort(testMth);
        Collections.sort(befClassMth);
        Collections.sort(befMth);
        Collections.sort(aftClassMth);
        Collections.sort(aftMth);
        Collections.sort(proptMth);

        rel.put("testMth", testMth);
        rel.put("befClassMth", befClassMth);
        rel.put("befMth", befMth);
        rel.put("aftClassMth", aftClassMth);
        rel.put("aftMth", aftMth);
        rel.put("proptMth", proptMth);
        return rel;
}
    //invoke all instance method
    private static void invokeMths(Map<String, Method> mthMap, Object instance, List<String> mths) {
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
    private static void invokeMths(Map<String, Method> mthMap, Class c, List<String> mths) {
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
    /*//a int range from min to max, inclusive
    private static Integer getRandomIntInRange(int min, int max){
        Random r = new Random();
        return min + r.nextInt(max - min + 1);
    }

*/











}