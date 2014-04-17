package ch.lws.demo.activiti.domain;

import java.io.Serializable;

public class Order implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;

	private Address address;

	public Order(long id, Address address) {
		this.id = id;
		this.setAddress(address);
	}

	public long getId() {
		return id;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "Order [id=" + id + ", address=" + address + "]";
	}

}
