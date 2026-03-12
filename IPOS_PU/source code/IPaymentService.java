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
	string passPayment(string merchantID, string orderID, string fullName, string address, string[] cardDetails, double amount);

}