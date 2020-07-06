package com.mse.personal.finance.db.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Objects;

/**
 * Base persistence entity taking care of primary identifier generation.
 *
 * @author T. Dossev
 */
@MappedSuperclass
public abstract class BaseEntity 
	implements Serializable 
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/*
	 * Getters & setters
	 */
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/*
	 * Other methods
	 */
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BaseEntity that = (BaseEntity) o;
		return Objects.equals(id, that.id);
	}
	
}