package ipos.pu.code;

public interface IPaymentService {

	/**
	 * 
	 * @param merchantID
	 * @param orderID
	 * @param fullName
	 * @param address
	 * @param cardDetails
	 * @param amount
	 */
	String passPayment(String merchantID, String orderID, String fullName, String address, String[] cardDetails, double amount);

}