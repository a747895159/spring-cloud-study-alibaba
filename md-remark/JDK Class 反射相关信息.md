
# 1.注解含义

    ```
        // Repeatable: 一个注解的标记，可以重复的加在同一个目标。  多个Role 会映射到 RoleUser.value里面
        @Repeatable(RoleUser.class)
        // Inherited: 继承意思，其标记的类 可以被继承 只能发生在类上，而不能发生在接口上
        @Inherited
        @RequestMapping
        public @interface Role {
            String name() default "普通学生";
        }
        
        @Role(name = "小组长")
        @Role(name = "小组长2")
        @Role(name = "小组长3")
        public class AnnotationTest {
        }
    ```

    |	-------                                     |编写自定义注解时未写@Inherited的运行结果：	|编写自定义注解时写了@Inherited的运行结果：
    |子类的类上能否继承到父类的类上的注解？	                |否	                                    |能
    |子类方法，实现了父类上的抽象方法，这个方法能否继承到注解？	|否	                                    |否
    |子类方法，继承了父类上的方法，这个方法能否继承到注解？	|能	                                    |能
    |子类方法，覆盖了父类上的方法，这个方法能否继承到注解？	|否	                                    |否

# 2.JDK8 获取类的信息
    - 获取类构造器
        - Connstructor<T> getConstructor(Class<?>...parameterTypes):返回此Class对象对应类的带指定形参的public构造器
        - Constructor<?>[] getConstructors():返回此Class对象对应类的所有public构造器
        - Constructor<T>[] getDeclaredConstructor(Class<?>...parameterTypes):返回此class对象对应类的带指定参数的构造器，与构造器的访问权限无关
        - Constructor<?>[] getDeclaredConstructors():返回此class对象对应类的所有构造器，与构造器的访问权限无关
    - 获取类成员方法
        - Method getMethod(String name,Class<?>...parameterTypes):返回此class对象对应类的带指定形参的public方法
        - Method[] getMethods():返回此class对象所表示的类的所有public方法
        - Method getDeclaredMethod(string name,Class<?>...parameterTypes):返回此class对象对应类的带指定形参的方法，与方法访问权限无关
        - Method[] getDeclaredMethods():返回此class对象对应类的全部方法，与方法的访问权限无关
    - 获取类成员变量
        - Field getField(String name):返回此class对象对应类的指定名称的public成员变量
        - Field[] getFields():返回此class对象对应类的所有public成员变量
        - Field getDeclaredField(String name):返回此class对象对应类的指定名称的成员变量，与成员变量访问权限无关
        - Field[] getDeclaredFields():返回此class对象对应类的全部成员变量，与成员变量的访问权限无关
    - **获取类注解**
        - <A extends Annotation>A getAnnotation(Class<A>annotationClass):尝试获取该class对象对应类上村子的指定类型的Annotation，如果该类型注解不存在，则返回null
        - <A extends Annotation>A getDeclaredAnnotation(Class<A>annotationClass):这是Java 8中新增的，该方法获取直接修饰该class对象对应类的指定类型的Annotation，如果不存在，则返回null
        - Annotation[] getAnnotations():返回修饰该class对象对应类上存在的所有Annotation
        - Annotation[] getDeclaredAnnotations():返回修饰该Class对象对应类上存在的所有Annotation
        - <A extends Annotation>A[] getAnnotationByType(Class<A>annotationClass):该方法的功能与前面介绍的getAnnotation()方法基本相似，但由于Java8增加了重复注解功能，因此需要使用该方法获取修饰该类的指定类型的多个Annotation
        - <A extends Annotation>A[] getDeclaredAnnotationByType(Class<A>annotationClass):该方法发功能与前面介绍的getDeclaredAnnotations()方法相似，也是因为Java8的重复注解的功能，需要使用该方法获取直接修饰该类的指定类型的多个Annotation
    - 获取该类内部类
        - Class<?>[] getDeclaredClasses():返回该class队形对应类里包含的全部内部类
    - 获取该类对象所在的外部类
        - Class<?> getDeclaringClass():返回该Class对象对应类所在的外部类
    - 获取该类对象对应类所实现的接口
        - Class<?>[] getInterfaces():返回该Class对象对应类所实现的全部接口
    - 获取该类对象对应类所继承的父类
        - Class<? super T> getSuperclass():返回该Class对象对应类的超类的Class对象
    - 获取该类对象对应类的修饰符、所在包、类名等基本信息
        - int getModifiers():返回此类或接口的所有修饰符，修饰符由public、protected、private、final、static、abstract等对应的常量组成，返回的整数应使用Modifier工具类的方法来解码，才可以获取真是的修饰符
        - Package getPackage():获取该类的包
        - String getName():以字符串形式返回此CLass对象所表示的类的简称
    - 判断该类是否为接口、枚举、注解类型
        - boolean isAnnotation():返回此class对象是否表示一个注解类型
        - boolean isAnnotationPresent(Class<? extends Annotation>annotationClass):判断此Class对象是否使用类Annotation修饰
        - boolean isAnonymousClass():返回此class对象是否是一个匿名类
        - boolean isArray():返回此class对象是否表示一个数组类
        - boolean isEnum():返回此class对象是否表示一个枚举
        - boolean isInterface():返回此class对象是否表示一个接口
        - boolean isInstance(Object obj):判断obj是否是此class对象的实例，该方法可以完全代替instanceof操作符


# 3.Spring @RequestMapping 注解在接口上， 为啥子类也能获取到？ @RequestMapping注解上没有 @Inherited注解

    - Spring内部自定义了一套注解解决方式通过 AnnotationUtils.findAnnotation(),可以是Class、Method 都可以获取到接口类的注解
    - 备注：哪怕@MyAnno上没有标注@Inherited，也是能找出来的（这是后面讲解@RequestMapping为何能被子类继承的重要原因）

