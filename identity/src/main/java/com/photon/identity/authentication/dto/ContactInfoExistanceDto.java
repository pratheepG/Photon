/**
 * 
 */
package com.photon.identity.authentication.dto;

/**
 * @author pratheepg
 *
 */
public class ContactInfoExistanceDto {

	private Boolean isDuplicatedPhone;
	private Boolean isDuplicateEmail;
	
	/**
	 * @return the isDuplicatedPhone
	 */
	public Boolean getIsDuplicatedPhone() {
		return isDuplicatedPhone;
	}
	/**
	 * @param isDuplicatedPhone the isDuplicatedPhone to set
	 */
	public void setIsDuplicatedPhone(Boolean isDuplicatedPhone) {
		this.isDuplicatedPhone = isDuplicatedPhone;
	}
	/**
	 * @return the isDuplicateEmail
	 */
	public Boolean getIsDuplicateEmail() {
		return isDuplicateEmail;
	}
	/**
	 * @param isDuplicateEmail the isDuplicateEmail to set
	 */
	public void setIsDuplicateEmail(Boolean isDuplicateEmail) {
		this.isDuplicateEmail = isDuplicateEmail;
	}
	/**
	 * @param isDuplicatedPhone
	 * @param isDuplicateEmail
	 */
	public ContactInfoExistanceDto(Boolean isDuplicatedPhone, Boolean isDuplicateEmail) {
		super();
		this.isDuplicatedPhone = isDuplicatedPhone;
		this.isDuplicateEmail = isDuplicateEmail;
	}
	/**
	 * 
	 */
	public ContactInfoExistanceDto() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "ContactInfoExistanceDto [isDuplicatedPhone=" + isDuplicatedPhone + ", isDuplicateEmail="
				+ isDuplicateEmail + "]";
	}
	
}
