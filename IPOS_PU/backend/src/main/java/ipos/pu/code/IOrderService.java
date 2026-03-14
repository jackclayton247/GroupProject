package ipos.pu.code;

public interface IOrderService {

	/**
	 * 
	 * @param merchantID
	 * @param orderDetails
	 */
	String placeRestockOrder(String merchantID, String orderDetails);

	/**
	 * 
	 * @param orderID
	 */
	String trackDelivery(String orderID);

	/**
	 * 
	 * @param merchantID
	 */
	double queryOutstandingBalance(String merchantID);

	/**
	 * 
	 * @param orderID
	 */
	String getInvoice(String orderID);

	/**
	 * 
	 * @param merchantID
	 * @param status
	 */
	boolean getAccStatus(String merchantID, String status);

	/**
	 * 
	 * @param merchantID
	 */
	boolean viewDiscountPlan(String merchantID);

	/**
	 * 
	 * @param merchantID
	 */
	boolean viewCreditLimit(String merchantID);

}