package ipos.pu.code;

public interface ISMTPConnection {

	/**
	 * 
	 * @param email Email address of addressee
	 * @param content
	 * @param reference
	 * @param sender
	 */
	boolean sendEmail(String email, String content, String reference, String sender);

}