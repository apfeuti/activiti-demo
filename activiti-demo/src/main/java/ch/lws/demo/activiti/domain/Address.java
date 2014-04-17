package ch.lws.demo.activiti.domain;

import java.io.Serializable;

public class Address implements Serializable {

	private static final long serialVersionUID = 1L;

	private String organisationName;

	private String zip;

	private String loadingPointZip;

	public Address(String organisationName, String zip, String loadingPointZip) {
		this.organisationName = organisationName;
		this.zip = zip;
		this.setLoadingPointZip(loadingPointZip);
	}

	public String getOrganisationName() {
		return organisationName;
	}

	public void setOrganisationName(String organisationName) {
		this.organisationName = organisationName;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getLoadingPointZip() {
		return loadingPointZip;
	}

	public void setLoadingPointZip(String loadingPointZip) {
		this.loadingPointZip = loadingPointZip;
	}

	@Override
	public String toString() {
		return "Address [organisationName=" + organisationName + ", zip=" + zip
				+ ", loadingPointZip=" + loadingPointZip + "]";
	}

}
