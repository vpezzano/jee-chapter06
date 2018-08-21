package model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

/*
 * In MySQL, the table structure can be seen as follows:
 * SHOW CREATE TABLE <tablename>.
 */
@Entity
public class CD {
	@Id
	@GeneratedValue
	private Long id;
	private String title;
	private Float price;
	private String description;

	public CD() {
	}

	public CD(String title, Float price, String description) {
		super();
		this.title = title;
		this.price = price;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void increasePrice(float increase) {
		this.price += increase;
	}

	@Override
	public String toString() {
		return "CD [id=" + id + ", title=" + title + ", price=" + price + ", description=" + description + "]";
	}
}