package ch.lws.demo.activiti;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ConditionDeterminer implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final List<String> companiesRequireApproval = Arrays.asList(
			"UBS", "Roche", "Migros");

	public static boolean isExplicitApprovalRequired(String organisationName) {
		if (organisationName == null || organisationName.length() == 0) {
			return false;
		}
		for (String current : companiesRequireApproval) {
			if (organisationName.toLowerCase().contains(current.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isSCFWest(String zip, String zipLoadingPoint) {
		int relevantZip = zipLoadingPoint != null ? Integer
				.valueOf(zipLoadingPoint) : Integer.valueOf(zip);
		return relevantZip < 3000;
	}

	public static boolean isSCFMiddle(String zip, String zipLoadingPoint) {
		int relevantZip = zipLoadingPoint != null ? Integer
				.valueOf(zipLoadingPoint) : Integer.valueOf(zip);
		return relevantZip >= 3000 && relevantZip < 6000;
	}

	public static boolean isSCFSouth(String zip, String zipLoadingPoint) {
		int relevantZip = zipLoadingPoint != null ? Integer
				.valueOf(zipLoadingPoint) : Integer.valueOf(zip);
		return relevantZip >= 6000 && relevantZip < 7000;
	}

	public static boolean isSCFEast(String zip, String zipLoadingPoint) {
		int relevantZip = zipLoadingPoint != null ? Integer
				.valueOf(zipLoadingPoint) : Integer.valueOf(zip);
		return relevantZip >= 7000;
	}
}
