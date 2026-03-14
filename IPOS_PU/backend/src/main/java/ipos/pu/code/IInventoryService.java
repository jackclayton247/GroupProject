package ipos.pu.code;

import java.util.List;

public interface IInventoryService {

	List<CatalogueItem> getCatalogue();

	/**
	 * 
	 * @param itemID
	 * @param quantity
	 */
	String reserveItemsForPurchase(String[] itemID, int[] quantity);

	/**
	 * 
	 * @param reservationID
	 */
	boolean cancelReservedItems(String[] reservationID);

	/**
	 * 
	 * @param reservationID
	 */
	String deductStock(String reservationID);

}