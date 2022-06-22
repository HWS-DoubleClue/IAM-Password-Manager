package javax.annotation;
public @interface Generated {
    String[] value();

    String date() default "";

    String comments() default "";
}