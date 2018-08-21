package main;

import java.util.Date;
import java.util.HashMap;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.Persistence;

import model.Address;
import model.Book;
import model.CD;
import model.Customer;

/*
 * Detailed info on locking is here: http://lostincoding.blogspot.com/2015/11/differences-in-jpa-entity-locking-modes.html
 */
public class Main {
	private static EntityManagerFactory emf;

	public static void main(String[] args) throws InterruptedException {
		// Generate schema
		Persistence.generateSchema("chapter06PU", new HashMap<Object, Object>());

		// 1-Creates instances of objects to persist
		Customer customer = new Customer("Antony", "Bandit", "tballa@mail.com", 20, "M");
		Address address = new Address("Ritherdon Rd", "London", "8QE", "UK");
		customer.setAddress(address);

		// 2-Obtains an entity manager and a transaction
		emf = Persistence.createEntityManagerFactory("chapter06PU");
		EntityManager em = emf.createEntityManager();

		// 3-Persists the objects to the database
		EntityTransaction tx = em.getTransaction();

		try {
			tx.begin();

			em.persist(address);

			// The following will push the changes to the database, although they will
			// be visible only after commit. The usage of flush is for cases in which
			// we have a lot of objects in the first-level cache and this would lead
			// to an OutOfMemoryException
			em.flush();
			em.clear();

			// The following will now generate an exception, because of a duplicate key
			// due to cascading the insertion of address. Address has already previously
			// flushed to the database, and so persisting the customer will duplicate
			// the insertion of address
			em.persist(customer);
			tx.commit();
		} catch (Exception e) {
			// When using InnoDB engine, an exception will roll-back anyway
			// the entire transaction, also without explicitly calling the
			// roll-back method
			if (tx.isActive()) {
				tx.rollback();
			}
		}

		tx = em.getTransaction();
		tx.begin();
		em.persist(customer);
		em.persist(address);
		tx.commit();

		// If for some reason here we detach, we cannot use
		// later on persist, because this would yield a
		// duplicate key exception; in order to avoid this,
		// we need to use then merge
		em.detach(customer);

		System.out.println("em.contains(customer): " + em.contains(customer));

		// Use the merge operation to re-attach an entity
		customer.setFirstName("Alexander");

		tx = em.getTransaction();
		tx.begin();
		em.merge(customer);
		tx.commit();

		// Here, removing the customer will remove also the address, because of
		// the orphanRemoval setting
		tx = em.getTransaction();
		tx.begin();
		customer = em.find(Customer.class, customer.getId());
		em.remove(customer);
		tx.commit();

		// The persist marks an entity for synchronization with the database
		// at commit time. So, any change to the entity done before committing
		// (like updating the first name) will also be synchronized with the
		// database
		customer = new Customer("Ellen", "Lemon", "lemon@mail.com", 25, "F");
		customer.setAddress(address);
		tx.begin();
		em.persist(address);
		em.persist(customer);
		customer.setFirstName("Barbara");

		// Now that cascade has been added, we could remove persisting
		// the address from the previous code
		customer = new Customer("Sandy", "Beam", "sbean@mail.com", 27, "M");
		address = new Address("Voorstraat 15", "Utrecht", "3546AB", "NL");
		customer.setAddress(address);
		em.persist(customer);
		tx.commit();

		// 4-Reads the objects from the database
		em.close();
		em = emf.createEntityManager();

		customer = em.find(Customer.class, customer.getId());
		customer.setFirstName("William");

		em.refresh(customer);

		System.out.println("After refresh, firstName = " + customer.getFirstName());

		if (em.contains(customer)) {
			System.out.println("EntityManager contains " + customer);
		}

		// The method detach removes an object from the persistence context, but
		// the object is still in the database. To remove all the objects from the
		// persistence context, use clear. The remove method would also delete the
		// entity from the database
		em.detach(customer);

		if (!em.contains(customer)) {
			System.out.println("After detach EntityManager does not contain " + customer);
		}

		// Some more inserts, necessary for testing MainJPQL
		tx = em.getTransaction();
		tx.begin();

		customer = new Customer("Mandy", "Bunnik", "mbunnik@mail.com", 23, "F");
		address = new Address("Leidseplein 15", "Amsterdam", "2010FT", "NL");
		customer.setAddress(address);
		em.persist(customer);

		customer = new Customer("Fred", "Kroon", "fkroon@mail.com", 41, "M");
		address = new Address("Venice Street 25", "Manchester", "1234", "UK");
		customer.setAddress(address);
		em.persist(customer);

		customer = new Customer("Antonio", "Rossi", "arossi@mail.com", 50, "M");
		address = new Address("Piazza Dante 72", "Roma", "2367", "IT");
		customer.setAddress(address);
		em.persist(customer);

		tx.commit();

		// This will be false, because of setting
		// eclipselink.persistence-context.close-on-commit to true
		System.out.println("em.contains(customer): " + em.contains(customer));

		// We enabled the caching for Customer; this will be true
		Cache cache = emf.getCache();
		System.out.println("cache.contains(Customer.class, customer.getId()): "
				+ cache.contains(Customer.class, customer.getId()));

		tx.begin();

		customer = em.find(Customer.class, customer.getId());
		customer.setEmail("arossi@yahoo.com");
		em.persist(customer);

		tx.commit();

		// Here we can see that the query to get the Customer object is not issued,
		// because the entity is cacheable, and so it's stored in the second-level
		// cache. The query to get the Address object is instead issued.
		// If we change in Customer class, configuring @Cacheable(false), the
		// entity manager will need to issue a query to the database also for
		// the Customer object
		Customer customerFromCache = em.find(Customer.class, customer.getId());
		System.out.println("Customer object from cache: " + customerFromCache);

		// After eviction, the object is not anymore in the second-level cache
		cache.evict(Customer.class);
		System.out.println("cache.contains(Customer.class, customer.getId()): "
				+ cache.contains(Customer.class, customer.getId()));

		tx.begin();

		// Create a book, to be used for testing concurrency
		Book book = new Book("H2G2", 20f, "Best IT book", "123-456", 321, false);

		em.persist(book);

		tx.commit();

		BookPriceModifier bookPriceModifier1 = new BookPriceModifier(book.getId(), 5f);
		BookPriceModifier bookPriceModifier2 = new BookPriceModifier(book.getId(), 10f);

		Thread tBook1 = new Thread(bookPriceModifier1);
		tBook1.start();

		Thread tBook2 = new Thread(bookPriceModifier2);
		tBook2.start();

		// Wait for threads to finish
		tBook1.join();
		tBook2.join();

		tx.begin();

		// Create a cd, to be used for testing concurrency
		CD cd = new CD("Sweet Dreams", 25f, "Eurithmics's Best Album");

		em.persist(cd);

		tx.commit();

		CDPriceModifier cdPriceModifier = new CDPriceModifier(cd.getId(), 5f);
		CDPessimisticReadTester cdPessimisticReadTester = new CDPessimisticReadTester(cd.getId());

		Thread tCDPriceModifier = new Thread(cdPriceModifier);
		tCDPriceModifier.start();

		// Let's delay the reading of 3 seconds, so that we make sure the
		// update executed in CPPriceModifier will start early
		Thread tCDPessimisticReadTester = new Thread(cdPessimisticReadTester);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		tCDPessimisticReadTester.start();

		// Wait for threads to finish
		tCDPriceModifier.join();
		tCDPessimisticReadTester.join();

		em.close();
		emf.close();

		System.exit(0);
	}

	private static class BookPriceModifier implements Runnable {
		private long id;
		private float price;

		private BookPriceModifier(long id, float price) {
			this.id = id;
			this.price = price;
		}

		@Override
		public void run() {
			EntityManager em = emf.createEntityManager();

			try {

				EntityTransaction tx = em.getTransaction();

				System.out.println("Started modifying Book object at " + new Date() + ", transaction: " + tx);
			
				tx.begin();

				// Using the @Version annotation on the Book entity, automatically ensures the
				// optimistic locking. But, to control the point where the locking happens,
				// we can do it explicitly, either via a read and lock (lock as you read):
				// Book book = em.find(Book.class, id, LockModeType.OPTIMISTIC);
				// or via a read and then lock:
				// Book book = em.find(Book.class, id);
				// em.lock(book, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
				// LockModeType.OPTIMISTIC_FORCE_INCREMENT forces a version increase at commit
				// time, even if there are no changes on the entity
				Book book = em.find(Book.class, id);
				if (book != null) {
					book.increasePrice(price);
				}

				tx.commit();
				
				System.out.println("Ended modifying Book object at " + new Date() + ", transaction: " + tx);

			} catch (Exception e) {
				System.out.println("While increasing the price of " + price + ", the following problem appeared: "
						+ e.getMessage());
			} finally {
				em.close();
			}
		}
	}

	private static class CDPriceModifier implements Runnable {
		private long id;
		private float price;

		private CDPriceModifier(long id, float price) {
			this.id = id;
			this.price = price;
		}

		@Override
		public void run() {
			EntityManager em = emf.createEntityManager();
			EntityTransaction tx = em.getTransaction();

			System.out.println("Started modifying CD object at " + new Date());
			
			tx.begin();

			// We can either do a read and lock (lock as you read):
			// CD cd = em.find(CD.class, id, LockModeType.PESSIMISTIC_WRITE);
			// or a read and then lock:
			// CD cd = em.find(CD.class, id);
			// em.lock(cd, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
			// LockModeType.PESSIMISTIC_FORCE_INCREMENT forces a version increase
			// at commit time, even if there are no changes on the entity.
			// PESSIMISTIC_READ: The entity manager locks the entity, preventing
			// it from being updated or deleted, but still allowing to read it.
			// PESSIMISTIC_WRITE: this is a stronger version of
			// LockModeType.PESSIMISTIC_READ. When WRITE lock is in place,
			// no other transaction can even read the entity
			CD cd = em.find(CD.class, id, LockModeType.PESSIMISTIC_WRITE);
			if (cd != null) {
				try {
					cd.increasePrice(price);
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			tx.commit();
			
			System.out.println("Ended modifying CD object at " + new Date() + ": " + cd);
			
			em.close();
		}
	}

	private static class CDPessimisticReadTester implements Runnable {
		private long id;

		private CDPessimisticReadTester(long id) {
			this.id = id;
		}

		@Override
		public void run() {
			EntityManager em = emf.createEntityManager();

			EntityTransaction tx = em.getTransaction();

			System.out.println("Started reading CD object at " + new Date());
			
			tx.begin();
			
			// If the CDPriceModifier has PESSIMISTIC_WRITE, here we
			// can use PESSIMISTIC_WRITE or PESSIMISTIC_READ, but the
			// behavior will be the same (it will wait for the
			// update in CDPriceModifier to complete). On the contrary,
			// if the CDPriceModifier has PESSIMISTIC_READ, here we
			// should be able to read, but it's not guaranteed, and
			// it depends on the implementation
			CD cd = em.find(CD.class, id, LockModeType.PESSIMISTIC_READ);

			tx.commit();

			System.out.println("Ended reading CD object at " + new Date() + ": " + cd);

			em.close();

		}
	}
}
