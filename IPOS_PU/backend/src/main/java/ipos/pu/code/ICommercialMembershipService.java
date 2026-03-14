package ipos.pu.code;

public interface ICommercialMembershipService {

	/**
	 * Allows PU to pass an application for a commercial member to SA
	 * @param companyName
	 * @param resgistrationNumber
	 * @param directors Details on company directors.
	 * String stores an array-like structure: [Name, Role, Email; Name, Role, Email] to facilitate InfoPharma staff diligence checks.
	 * @param businessType
	 * @param address
	 * @param email
	 * @param fax
	 * @param preferPhysicalMail True if commercial customer prefers to receive the confirmation by regular mail
	 */
	boolean requestMembership(String companyName, String registrationNumber, String directors, String businessType, String address, String email, String fax, boolean preferPhysicalMail);

}