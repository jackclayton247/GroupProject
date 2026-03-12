public interface IPaymentProcessing {

	/**
	 * 
	 * @param merchantID
	 * @param orderID
	 * @param fullName
	 * @param address
	 * @param cardDetails
	 * @param amount
	 */
	string requestPayment(string merchantID, string orderID, string fullName, string address, string[] cardDetails, double amount);

}