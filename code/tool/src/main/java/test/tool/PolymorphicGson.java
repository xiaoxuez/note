package test.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Gson解多态, RuntimeTypeAdapterFactory在Gson的github上，但不在Gson包里
 */
public class PolymorphicGson {

    public class Animal {
        public String name;
        // this specifies which animal it is
        public A a;
    }

    public class A {
        public String type;
    }

    public class Dog extends A {
        public boolean playsCatch;
    }

    public class Cat extends A {
        public boolean chasesRedLaserDot;
    }

    public static void main(String[] args) {
        final TypeToken<Animal> requestListTypeToken = new TypeToken<Animal>() {};

// adding all different container classes with their flag
        final RuntimeTypeAdapterFactory typeFactory = RuntimeTypeAdapterFactory
                .of(A.class, "type") // Here you specify which is the parent class and what field particularizes the child class.
                .registerSubtype(Dog.class, "dog") // if the flag equals the class name, you can skip the second parameter. This is only necessary, when the "type" field does not equal the class name.
                .registerSubtype(Cat.class, "cat");
        final Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();
        Animal a = gson.fromJson("{\"name\": \"test\", \"a\":{\"type\": \"dog\", \"playsCatch\": false}}", requestListTypeToken.getType() );
        Dog dog = (Dog) a.a;
        System.out.println(dog.playsCatch);
    }
}
