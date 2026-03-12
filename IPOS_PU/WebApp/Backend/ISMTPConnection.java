public interface ISMTPConnection {

	/**
	 * 
	 * @param email Email address of addressee
	 * @param content
	 * @param reference
	 * @param sender
	 */
	boolean sendEmail(string email, string content, string reference, string sender);

}