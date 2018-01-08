import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.MinguoDate;
import java.time.temporal.TemporalField;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by xiaoxuez on 2017/11/16.
 */
public class Java8Test {


    public static class LambdaTest {

        public void testArray() {
            //排序
            List<Integer> x = Arrays.asList(5, 3, 2, 8, 1);
            x.sort((a, b) -> {
                return a - b;
            });
            x.forEach((a) -> System.out.println(a));
        }

        /**
         * Supplier接口主要用于方法无参数有返回值，调用如create(LambdaTest::new)
         * @param supplier
         * @return
         */
        public static LambdaTest create( final Supplier< LambdaTest > supplier ) {
            return supplier.get();
        }

        public static void collide( final LambdaTest car ) {
            System.out.println( "Collided " + car.toString() );
        }

        public void follow( final LambdaTest another ) {
            System.out.println( "Following the " + another.toString() );
        }

        public void repair() {
            System.out.println( "Repaired " + this.toString() );
        }


        public static void main(String[] args) {
            LambdaTest lambdaTest = new LambdaTest();
            lambdaTest.testArray();

            final LambdaTest t1 = LambdaTest.create( LambdaTest::new );
            System.out.println("addr t1: " + t1);

            LambdaTest t2 = new LambdaTest();
            System.out.println("addr t2: " + t2);

            final List< LambdaTest > ts = Arrays.asList( t1 );
            ts.forEach(LambdaTest::repair);
            ts.forEach(LambdaTest::collide);
            ts.forEach(t2::follow);
        }
    }


    /**
     * 泛型拓宽，GenericTest.defaultValue()可自动推导出类型String
     * @param <T>
     */
    public static class GenericTest <T> {
        public static<T> T defaultValue() {
            return null;
        }

        //3个T， 第一个T确定了，其余两个都可以自动推导出了
        public T getOrDefault( T value, T defaultValue ) {
            return ( value != null ) ? value : defaultValue;
        }

        public static void main(String[] args) {
            final GenericTest< String > value = new GenericTest<>();
            value.getOrDefault( "22", GenericTest.defaultValue() );
        }
    }


    public static class NewDateClass {
        public void testClock() {
            Clock clock = Clock.systemUTC();
            System.out.println( "UTC ZONE: "+clock.instant() );

//            System.out.println(Clock.system.instant());
            System.out.println(System.currentTimeMillis());
            System.out.println(clock.millis());
            System.out.println(new Date().getTime());

        }

        /**
         * 主要是LocalDateTime的使用
         */
        public void testLocalDateTime() {
            LocalDateTime localDateTime = LocalDateTime.now();
            System.out.println(localDateTime);
            System.out.println(localDateTime.getMonthValue());
            //星期减1个星期,返回新的对象，原对象不变
            System.out.println(localDateTime.minusWeeks(1));
            //星期加1个星期,返回新的对象，原对象不变
            System.out.println(localDateTime.plusWeeks(1));
//            ChronoLocalDate date = ChronoLocalDate.
            MinguoDate d = MinguoDate.now();
//            d.
        }

        public static void main(String[] args) {
            String x = "2461,2462,2463,2464,2465,2466,2467,2468,2470,2471,2472,2473,2474,2475,2476,2477,2478,2480,2481,2602";
            String[] x1 = x.split(",");
            String y= "";
            System.out.println(x1.length);
            for (int i=0;i<x1.length;i++) {
                y += x1[i] + " OR ";
            }
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 25);
            System.out.println(c.get(Calendar.DAY_OF_WEEK));
        }
    }



}
