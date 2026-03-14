package ipos.pu.code;

public interface IOrderStatus {

	/**
	 * Allows CA staff to see status of a specific order made via the PU portal (status can be shipping, in transit or delivered)
	 * @param orderID
	 */
	String getOrderStatus(String orderID);

	/**
	 * Allows CA staff to see all the orders that have not been delivered.
	 * Returns an array of order IDs and status (status is either received or dispatched)
	 */
	String[] getUndeliveredOrders();

}