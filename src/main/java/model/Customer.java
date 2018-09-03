package model;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.OneToOne;
import javax.persistence.ParameterMode;

@Entity
@Cacheable(true)
@NamedQueries({ @NamedQuery(name = Customer.FIND_ALL, query = "SELECT c FROM model.Customer c"),
		@NamedQuery(name = Customer.FIND_VINCENT, query = "SELECT c FROM model.Customer c WHERE c.firstName = 'Vincent'"),
		@NamedQuery(name = Customer.FIND_WITH_PARAM, query = "SELECT c FROM model.Customer c WHERE c.firstName = :fname") })
@NamedNativeQuery(name = Customer.NATIVE_FIND_ALL, query = "SELECT * FROM customer")
@NamedStoredProcedureQuery(name = Customer.ARCHIVE, procedureName = "archive_customer", parameters = {
		@StoredProcedureParameter(name = "p_email", mode = ParameterMode.IN, type = String.class) })
public class Customer {
	public static final String FIND_ALL = "Customer.findAll";
	public static final String NATIVE_FIND_ALL = "Customer.nativeFindAll";
	public static final String FIND_VINCENT = "Customer.findVincent";
	public static final String FIND_WITH_PARAM = "Customer.findWithParam";
	public static final String ARCHIVE = "Customer.archive";

	@Id
	@GeneratedValue
	private Long id;
	private String firstName;
	private String lastName;
	private int age;
	private String gender;
	private String email;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "address_fk", nullable = false)
	private Address address;

	public Customer() {
	}

	public Customer(String firstName, String lastName, String email, int age, String gender) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.gender = gender;
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "Customer [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", age=" + age
				+ ", gender=" + gender + ", email=" + email + ", address=" + address + "]";
	}
}
