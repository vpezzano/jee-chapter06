package main;

import java.util.List;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.ParameterMode;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import model.Customer;
import model.Customer_;

public class MainJPQL {

	public static void main(String[] args) {

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("chapter06JpqlPU");
		EntityManager em = emf.createEntityManager();

		Query query = em.createQuery("SELECT c FROM model.Customer c");
		List<?> result = query.getResultList();
		System.out.println(result);

		query = em.createQuery("SELECT c.firstName, c.lastName FROM model.Customer c");
		result = query.getResultList();
		System.out.println(result);

		query = em.createQuery(
				"SELECT CASE c.gender WHEN 'M' THEN 'Mr' ELSE 'Ms' END, c.firstName, c.lastName FROM model.Customer c");
		result = query.getResultList();
		System.out.println(result);

		query = em.createQuery("SELECT c.address.country FROM model.Customer c");
		result = query.getResultList();
		System.out.println(result);

		query = em
				.createQuery("SELECT NEW model.CustomerDTO(c.firstName, c.lastName, c.address) FROM model.Customer c");
		result = query.getResultList();
		System.out.println(result);

		query = em.createQuery("SELECT c FROM model.Customer c WHERE c.age NOT BETWEEN 27 and 30");
		result = query.getResultList();
		System.out.println(result);

		query = em.createQuery("SELECT c FROM model.Customer c WHERE c.address.country NOT IN ('UK')");
		result = query.getResultList();
		System.out.println(result);

		query = em.createQuery("SELECT c FROM model.Customer c WHERE c.address.country=?1");
		query.setParameter(1, "NL");
		result = query.getResultList();
		System.out.println(result);

		query = em.createQuery("SELECT c FROM model.Customer c WHERE c.address.country=:country");
		query.setParameter("country", "UK");
		result = query.getResultList();
		System.out.println(result);

		query = em.createQuery(
				"SELECT c FROM model.Customer c WHERE c.age = (SELECT MIN(cust.age) FROM model.Customer cust)");
		result = query.getResultList();
		System.out.println(result);

		query = em.createQuery(
				"SELECT c.address.country, count(c) FROM model.Customer c GROUP BY c.address.country HAVING c.address.country != 'UK'");
		result = query.getResultList();
		print(result);

		TypedQuery<Customer> typedQuery = em
				.createQuery("SELECT c FROM model.Customer c WHERE c.address.country=:country", Customer.class);
		typedQuery.setParameter("country", "UK");
		List<Customer> typedResult = typedQuery.getResultList();
		print(typedResult);

		Query queryTotal = em.createQuery("SELECT COUNT(c.id) FROM model.Customer c");
		long total = (long) queryTotal.getSingleResult();
		int pageSize = 2;
		int numChunks = (int) Math.ceil(((double) total) / pageSize);
		typedQuery = em.createQuery("SELECT c FROM model.Customer c", Customer.class);
		typedQuery.setMaxResults(pageSize);
		for (int i = 0; i < numChunks; i++) {
			int offset = i * pageSize;
			typedQuery.setFirstResult(offset);
			typedResult = typedQuery.getResultList();
			System.out.println("Rows having offset: " + offset);
			print(typedResult);
		}

		typedQuery = em.createNamedQuery(Customer.FIND_ALL, Customer.class);
		typedResult = typedQuery.getResultList();
		print(typedResult);

		typedQuery = em.createNamedQuery(Customer.FIND_WITH_PARAM, Customer.class);
		typedQuery.setParameter("fname", "Mandy");
		typedResult = typedQuery.getResultList();
		print(typedResult);

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Customer> criteriaQuery = builder.createQuery(Customer.class);
		Root<Customer> c = criteriaQuery.from(Customer.class);
		criteriaQuery.select(c).where(builder.and(builder.equal(c.get(Customer_.firstName), "Sandy"),
				(builder.gt(c.get(Customer_.age), 20))));
		typedQuery = em.createQuery(criteriaQuery);
		List<Customer> customers = typedQuery.getResultList();
		print(customers);

		// When using the meta-model, the cast is not anymore required
		CriteriaQuery<Integer> cq = builder.createQuery(Integer.class)
				.select(builder.max(c.get(Customer_.age)/* .as(Integer.class) */));
		query = em.createQuery(cq);
		Integer age = (Integer) query.getSingleResult();
		print(age);

		criteriaQuery.select(c).where(builder.greaterThan(c.get(Customer_.age).as(Integer.class), 40));
		typedQuery = em.createQuery(criteriaQuery);
		List<Customer> customersAbove40 = typedQuery.getResultList();
		print(customersAbove40);

		Query nativeQuery = em.createNativeQuery("SELECT * FROM customer", Customer.class);
		@SuppressWarnings("unchecked")
		List<Customer> customersFromNativeQuery = nativeQuery.getResultList();
		print(customersFromNativeQuery);

		// From the annotation, it knows this is a NamedNativeQuery
		TypedQuery<Customer> namedNativeQuery = em.createNamedQuery(Customer.NATIVE_FIND_ALL, Customer.class);
		List<Customer> customersFromNamedNativeQuery = namedNativeQuery.getResultList();
		print(customersFromNamedNativeQuery);

		StoredProcedureQuery storedProcedureQuery = em.createNamedStoredProcedureQuery(Customer.ARCHIVE);
		storedProcedureQuery.setParameter("p_email", "lemon@mail.com");
		storedProcedureQuery.execute();

		storedProcedureQuery = em.createStoredProcedureQuery("archive_customer");
		storedProcedureQuery.registerStoredProcedureParameter("p_email", String.class, ParameterMode.IN);
		storedProcedureQuery.setParameter("p_email", "sbean@mail.com");
		storedProcedureQuery.execute();

		boolean bulkOperations = false;
		try {
			bulkOperations = Boolean.valueOf(args[0]);
		} catch (RuntimeException re) {
			bulkOperations = false;
		}

		if (bulkOperations) {
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			query = em.createQuery("UPDATE model.Customer c SET c.firstName = 'Young' WHERE c.age < 27");
			int intResult = query.executeUpdate();

			// By setting the flush mode to COMMIT on the transaction, the effect
			// of updates made to entities in the persistence context is not
			// defined by the specification, and the actual behavior is implementation
			// specific; so, we might not be able to see the changes in the following
			// query. When we use AUTO (default), the flush is done before executing
			// a query.
			query = em.createQuery("SELECT c.firstName, c.lastName FROM model.Customer c");
			
			result = query.getResultList();
			print(result);

			tx.commit();

			tx.begin();
			query = em.createQuery("DELETE FROM model.Customer c WHERE c.age < 27");
			intResult = query.executeUpdate();
			tx.commit();
			print(intResult);
		}

		// 2-Closes the entity manager and the factory
		em.close();
		emf.close();

		System.exit(0);
	}

	private static void print(Object resultObj) {
		if (resultObj instanceof List<?>) {
			List<?> result = (List<?>) resultObj;
			for (Object object : result) {
				if (object instanceof Object[]) {
					Object[] arrayObj = (Object[]) object;
					Stream.of(arrayObj).forEach(el -> System.out.print(el + "  "));
					System.out.println();
				} else {
					System.out.println(object);
				}
			}
		} else {
			System.out.println(resultObj);
		}
	}
}
