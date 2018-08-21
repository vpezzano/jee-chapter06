package model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.7.2.v20180622-rNA", date="2018-08-16T22:15:33")
@StaticMetamodel(Book.class)
public class Book_ { 

    public static volatile SingularAttribute<Book, Boolean> illustrations;
    public static volatile SingularAttribute<Book, Float> price;
    public static volatile SingularAttribute<Book, String> isbn;
    public static volatile SingularAttribute<Book, String> description;
    public static volatile SingularAttribute<Book, Long> id;
    public static volatile SingularAttribute<Book, String> title;
    public static volatile SingularAttribute<Book, Integer> nbOfPage;
    public static volatile SingularAttribute<Book, Integer> version;

}