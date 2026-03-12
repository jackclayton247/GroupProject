public interface IOrderService {

	/**
	 * 
	 * @param merchantID
	 * @param orderDetails
	 */
	string placeRestockOrder(string merchantID, string orderDetails);

	/**
	 * 
	 * @param orderID
	 */
	string trackDelivery(string orderID);

	/**
	 * 
	 * @param merchantID
	 */
	double queryOutstandingBalance(string merchantID);

	/**
	 * 
	 * @param orderID
	 */
	string getInvoice(string orderID);

	/**
	 * 
	 * @param merchantID
	 * @param status
	 */
	boolean getAccStatus(string merchantID, string status);

	/**
	 * 
	 * @param merchantID
	 */
	boolean viewDiscountPlan(string merchantID);

	/**
	 * 
	 * @param merchantID
	 */
	boolean viewCreditLimit(string merchantID);

}