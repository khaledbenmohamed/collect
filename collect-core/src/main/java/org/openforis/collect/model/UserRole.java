package org.openforis.collect.model;

public enum UserRole {
	
	VIEW(UserRoles.VIEW, 0),
	ENTRY(UserRoles.ENTRY, 1),
	CLEANSING(UserRoles.CLEANSING, 2),
	ANALYSIS(UserRoles.ANALYSIS, 3),
	ADMIN(UserRoles.ADMIN, 4);
	
	
	public static UserRole fromCode(String code) {
		for (UserRole role : values()) {
			if ( role.getCode().equals(code) ) {
				return role;
			}
		}
		throw new IllegalArgumentException(code + " is not a valid UserRole code");
	}

	private String code;
	private int hierarchicalOrder;

	UserRole(String code, int hierarchicalOrder) {
		this.code = code;
		this.hierarchicalOrder = hierarchicalOrder;
	}
	
	public String getCode() {
		return code;
	}
	
	public int getHierarchicalOrder() {
		return hierarchicalOrder;
	}
	
}