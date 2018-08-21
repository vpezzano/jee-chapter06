package model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import model.Address;

@Generated(value="EclipseLink-2.7.2.v20180622-rNA", date="2018-08-16T22:15:33")
@StaticMetamodel(Customer.class)
public class Customer_ { 

    public static volatile SingularAttribute<Customer, String> firstName;
    public static volatile SingularAttribute<Customer, String> lastName;
    public static volatile SingularAttribute<Customer, Address> address;
    public static volatile SingularAttribute<Customer, String> gender;
    public static volatile SingularAttribute<Customer, Long> id;
    public static volatile SingularAttribute<Customer, Integer> age;
    public static volatile SingularAttribute<Customer, String> email;

}