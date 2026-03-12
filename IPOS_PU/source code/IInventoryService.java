public interface IInventoryService {

	List<CatalogueItem> getCatalogue();

	/**
	 * 
	 * @param itemID
	 * @param quantity
	 */
	string reserveItemsForPurchase(string[] itemID, int[] quantity);

	/**
	 * 
	 * @param reservationID
	 */
	boolean cancelReservedItems(string[] reservationID);

	/**
	 * 
	 * @param reservationID
	 */
	string deductStock(string reservationID);

}